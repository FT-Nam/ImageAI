package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.request.PermissionRequest;
import com.ftnam.image_ai_backend.dto.response.PermissionResponse;
import com.ftnam.image_ai_backend.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}
