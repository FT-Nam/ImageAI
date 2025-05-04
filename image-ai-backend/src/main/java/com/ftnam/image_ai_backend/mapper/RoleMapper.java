package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.request.RoleRequest;
import com.ftnam.image_ai_backend.dto.response.RoleResponse;
import com.ftnam.image_ai_backend.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    Role toRole (RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
