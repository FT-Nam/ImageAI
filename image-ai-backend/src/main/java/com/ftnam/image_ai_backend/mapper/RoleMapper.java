package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.request.RoleRequest;
import com.ftnam.image_ai_backend.dto.response.RoleResponse;
import com.ftnam.image_ai_backend.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(source = "permissions", target = "permissions", ignore = true)
    Role toRole (RoleRequest request);

    @Mapping(source = "permissions", target = "permissions", ignore = true)
    RoleResponse toRoleResponse(Role role);
}
