package com.ftnam.image_ai_backend.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.ftnam.image_ai_backend.dto.response.FileData;
import com.ftnam.image_ai_backend.dto.response.FileResponse;
import com.ftnam.image_ai_backend.entity.FileMgmt;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.FileMgmtRepository;
import com.ftnam.image_ai_backend.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileCloudinaryServiceImpl implements FileService {
    Cloudinary cloudinary;
    FileMgmtRepository fileMgmtRepository;

    @Override
    public FileResponse uploadFile(MultipartFile file) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Map result = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "use_filename", true,
                        "unique_filename", true,
                        "folder", "pet_recognition",
                        "resource_type", "image"
                ));

        if(result.get("asset_id") == null || result.get("secure_url") == null){
            throw new AppException(ErrorCode.UPLOAD_FILE_FAILED);
        }


        FileMgmt fileMgmt = FileMgmt.builder()
                .id((String) result.get("asset_id"))
                .ownerId(userId)
                .size((Long) result.get("bytes"))
                .contentType(result.get("resource_type") + "/" + result.get("format"))
                .path((String) result.get("secure_url"))
                .build();

        fileMgmtRepository.save(fileMgmt);

        return FileResponse.builder()
                .fileId((String) result.get("asset_id"))
                .originalFileName((String) result.get("original_filename"))
                .url((String) result.get("secure_url"))
                .build();
    }

    @Override
    public FileData download(String fileId) throws IOException {
        return null;
    }

    @Override
    public void deleteFile(String fileId) throws IOException {
        if(!fileMgmtRepository.existsById(fileId)){
            throw new AppException(ErrorCode.FILE_NOT_FOUND);
        }

        Map result = cloudinary.uploader().destroy(fileId,
                ObjectUtils.asMap("asset_id", true));

        if ("ok".equals(result.get("result"))) {
            fileMgmtRepository.deleteById(fileId);
        } else {
            throw new AppException(ErrorCode.DELETE_FILE_FAILED);
        }
    }
}
