package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.RoleRequest;
import com.ftnam.image_ai_backend.dto.response.RoleResponse;

import java.util.List;

public interface RoleService {
    RoleResponse createRole(RoleRequest request);

    List<RoleResponse> getAll();

    void delete(String role);

}
