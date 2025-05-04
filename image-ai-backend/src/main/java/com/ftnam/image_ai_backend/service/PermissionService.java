package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.PermissionRequest;
import com.ftnam.image_ai_backend.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(PermissionRequest request);

    List<PermissionResponse> getAll();

    void delete(String name);
}
