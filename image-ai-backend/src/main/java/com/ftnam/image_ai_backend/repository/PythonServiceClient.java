package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.configuration.FeignMultipartSupportConfig;
import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(
        name = "pythonService",
        url = "http://localhost:5000",
        configuration = FeignMultipartSupportConfig.class
)
public interface PythonServiceClient {

    @PostMapping(value = "/predict", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AnalyzeResponse predict(@RequestPart("file") MultipartFile file);
}

