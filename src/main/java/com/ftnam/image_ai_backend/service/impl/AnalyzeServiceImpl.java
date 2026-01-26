package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.event.NotificationEvent;
import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.httpclient.PythonServiceClient;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.AnalyzeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AnalyzeServiceImpl implements AnalyzeService {
    FileServiceImpl fileService;
    HistoryServiceImpl historyService;
    PythonServiceClient pythonServiceClient;
    UserRepository userRepository;
    NotificationPublisher notificationPublisher;
    KafkaTemplate<String,Object> kafkaTemplate;


    @Override
    public AnalyzeResponse analyzeImage(MultipartFile file) throws IOException {
        var upload = fileService.uploadFile(file);

        AnalyzeResponse predict = pythonServiceClient.predict(file);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")){
            String userId = authentication.getName();

            User user = userRepository.findById(userId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

            if(user.getCredit() < 20){
                notificationPublisher.sendNotification(userId, "Insufficient credits to analyze image");

                throw new AppException(ErrorCode.NOT_ENOUGH_CREDITS);
            }

            user.setCredit(user.getCredit() - 20);

            HistoryRequest historyRequest = HistoryRequest.builder()
                    .imageUrl(upload.getUrl())
                    .animal(predict.getAnimal())
                    .animal_confidence(predict.getAnimal_confidence())
                    .breed(predict.getBreed())
                    .breed_confidence(predict.getBreed_confidence())
                    .status(predict.getStatus())
                    .userId(userId)
                    .build();

            notificationPublisher.sendNotification(userId, "Analyze image has been successfully");

            historyService.createHistory(historyRequest);
        }

        return AnalyzeResponse.builder()
                .imageUrl(upload.getUrl())
                .animal(predict.getAnimal())
                .animal_confidence(predict.getAnimal_confidence())
                .breed(predict.getBreed())
                .breed_confidence(predict.getBreed_confidence())
                .status(predict.getStatus())
                .build();
    }

}
