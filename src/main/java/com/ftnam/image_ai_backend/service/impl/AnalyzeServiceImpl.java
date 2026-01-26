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
import com.ftnam.image_ai_backend.service.FileService;
import com.ftnam.image_ai_backend.service.HistoryService;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AnalyzeServiceImpl implements AnalyzeService {
    FileService fileService;
    HistoryService historyService;
    PythonServiceClient pythonServiceClient;
    UserRepository userRepository;
    NotificationPublisher notificationPublisher;

    @NonFinal
    @Value("${app.analyze.credit-cost}")
    int creditCost;

    @Override
    @Transactional
    public AnalyzeResponse analyzeImage(MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        User user = null;
        String userId = null;
        
        if(authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")){
            userId = authentication.getName();
            user = userRepository.findById(userId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

            if(user.getCredit() < creditCost){
                notificationPublisher.sendNotification(userId, "Insufficient credits to analyze image");
                throw new AppException(ErrorCode.NOT_ENOUGH_CREDITS);
            }
        }

        var upload = fileService.uploadFile(file);
        String uploadedFileName = upload.getOriginalFileName();
        
        AnalyzeResponse predict;
        try {
            predict = pythonServiceClient.predict(file);
        } catch (Exception e) {
            log.error("Python service call failed: {}", e.getMessage(), e);
            // Rollback: Xóa file đã upload vì Python service fail
            try {
                fileService.deleteFile(uploadedFileName);
            } catch (IOException deleteException) {
                log.error("Failed to delete uploaded file {}: {}", uploadedFileName, deleteException.getMessage());
            }
            
            // Gửi notification cho user nếu có
            if(user != null){
                notificationPublisher.sendNotification(userId, 
                    "Image analysis failed due to service error. Please try again later.");
            }
            
            // Throw exception để rollback transaction (credit không bị trừ)
            throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
        }

        if(user != null){
            user.setCredit(user.getCredit() - creditCost);
            userRepository.save(user);

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
