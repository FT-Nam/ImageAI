package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import com.ftnam.image_ai_backend.repository.PythonServiceClient;
import com.ftnam.image_ai_backend.service.AnalyzeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    @Override
    public AnalyzeResponse analyzeImage(MultipartFile file) throws IOException {
        var upload = fileService.uploadFile(file);

        AnalyzeResponse predict = pythonServiceClient.predict(file);

        HistoryRequest historyRequest = HistoryRequest.builder()
                .imageUrl(upload.getUrl())
                .confident(predict.getAccuracy())
                .result(predict.getPrediction())
                .userId("47bbd017-4a0e-4df3-a29f-b25995fb4d65")
                .build();

        historyService.createHistory(historyRequest);

        return AnalyzeResponse.builder()
                .imageUrl(upload.getUrl())
                .accuracy(predict.getAccuracy())
                .description(predict.getDescription())
                .prediction(predict.getPrediction())
                .build();
    }
}
