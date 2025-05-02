package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.response.FileInfo;
import com.ftnam.image_ai_backend.entity.FileMgmt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileMgmtMapper {
    @Mapping(target = "id", source = "name")
    FileMgmt toFileMgmt(FileInfo fileInfo);
}
