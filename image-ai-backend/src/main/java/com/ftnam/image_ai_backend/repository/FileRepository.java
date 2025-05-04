package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.dto.response.FileInfo;
import com.ftnam.image_ai_backend.entity.FileMgmt;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Repository
public class FileRepository {
    @Value("${app.file.store-dir}")
    String storeDir;

    @Value("${app.file.download-prefix}")
    String urlPrefix;

    public FileInfo store(MultipartFile file) throws IOException {
        if(!Objects.requireNonNull(file.getContentType()).startsWith("image/")){
            throw new AppException(ErrorCode.INVALID_FILE_TYPE);
        }

        Path folder = Paths.get(storeDir);
        Files.createDirectories(folder);

        String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String fileName = (fileExtension == null)
                ? UUID.randomUUID().toString()
                : UUID.randomUUID() + "." +  fileExtension;

        Path filePath = folder.resolve(fileName).normalize().toAbsolutePath();

        byte[] bytes = file.getBytes();
        Files.write(filePath,bytes);

        return FileInfo.builder()
                .name(fileName)
                .size(file.getSize())
                .contentType(file.getContentType())
                .md5Checksum(DigestUtils.md5DigestAsHex(bytes))
                .path(filePath.toString())
                .url(urlPrefix + fileName)
                .build();
    }

    public Resource read(FileMgmt fileMgmt) throws IOException {
        var data = Files.readAllBytes(Path.of(fileMgmt.getPath()));
        return new ByteArrayResource(data);
    }
}
