package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    public FileResponse uploadFile(MultipartFile file) throws IOException;
}
