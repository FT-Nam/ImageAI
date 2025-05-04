package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.response.FileData;
import com.ftnam.image_ai_backend.dto.response.FileResponse;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.mapper.FileMgmtMapper;
import com.ftnam.image_ai_backend.repository.FileMgmtRepository;
import com.ftnam.image_ai_backend.repository.FileRepository;
import com.ftnam.image_ai_backend.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileServiceImpl implements FileService {
    FileRepository fileRepository;
    FileMgmtRepository fileMgmtRepository;
    FileMgmtMapper fileMgmtMapper;

    @Override
    public FileResponse uploadFile(MultipartFile file) throws IOException {
        // Store file
        var fileInfo = fileRepository.store(file);

        var fileMgmt = fileMgmtMapper.toFileMgmt(fileInfo);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        fileMgmt.setOwnerId(userId);

        fileMgmtRepository.save(fileMgmt);

        return FileResponse.builder()
                .originalFileName(file.getOriginalFilename())
                .url(fileInfo.getUrl())
                .build();
    }

    @Override
    public FileData download(String fileName) throws IOException {
        var fileMgmt = fileMgmtRepository.findById(fileName)
                .orElseThrow(() -> new AppException(ErrorCode.FILE_NOT_FOUND));

        var resource = fileRepository.read(fileMgmt);
        return new FileData(fileMgmt.getContentType(),resource);
    }
}
