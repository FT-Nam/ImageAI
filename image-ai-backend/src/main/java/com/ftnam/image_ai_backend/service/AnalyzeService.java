package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AnalyzeService {
    AnalyzeResponse analyzeImage(MultipartFile file) throws IOException;
}
