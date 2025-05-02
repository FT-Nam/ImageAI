package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.response.FileData;
import com.ftnam.image_ai_backend.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    FileResponse uploadFile(MultipartFile file) throws IOException;

    FileData download(String fileName) throws IOException;
}
