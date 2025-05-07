package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.PythonServiceClient;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.AnalyzeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
                throw new AppException(ErrorCode.NOT_ENOUGH_CREDITS);
            }

            user.setCredit(user.getCredit() - 20);

            HistoryRequest historyRequest = HistoryRequest.builder()
                    .imageUrl(upload.getUrl())
                    .confident(predict.getAccuracy())
                    .result(predict.getPrediction())
                    .userId(userId)
                    .build();

            historyService.createHistory(historyRequest);
        }

        return AnalyzeResponse.builder()
                .imageUrl(upload.getUrl())
                .accuracy(predict.getAccuracy())
                .description(predict.getDescription())
                .prediction(predict.getPrediction())
                .build();
    }
}
