package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.service.impl.AnalyzeServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/analyze")
public class AnalyzeController {
    AnalyzeServiceImpl analyzeService;

    @PostMapping
    ApiResponse<AnalyzeResponse> analyzeImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.<AnalyzeResponse>builder()
                .value(analyzeService.analyzeImage(file))
                .build();
    }
}
