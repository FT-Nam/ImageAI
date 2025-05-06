package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.FileResponse;
import com.ftnam.image_ai_backend.service.impl.FileServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/file")
public class FileController {
    FileServiceImpl fileService;

    @PostMapping("/upload")
    ApiResponse<FileResponse> uploadFile(@RequestPart("file") MultipartFile file) throws IOException {
        return ApiResponse.<FileResponse>builder()
                .value(fileService.uploadFile(file))
                .build();
    }

    @GetMapping("/download/{fileName}")
    ResponseEntity<Resource> downloadMedia(@PathVariable String fileName) throws IOException {
        var fileData = fileService.download(fileName);

        return ResponseEntity.<Resource>ok()
                .header(HttpHeaders.CONTENT_TYPE, fileData.contentType())
                .body(fileData.resource());
    }
}
