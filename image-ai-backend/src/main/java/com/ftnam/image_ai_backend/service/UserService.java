package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.UserCreationRequest;
import com.ftnam.image_ai_backend.dto.request.UserUpdateRequest;
import com.ftnam.image_ai_backend.dto.response.UserResponse;
import com.ftnam.image_ai_backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserResponse createUser(UserCreationRequest request);

    Page<UserResponse> getUsers(Pageable pageable);

    UserResponse getUserById(String id);

    UserResponse updateUser(String id, UserUpdateRequest request);
    void deleteUser(String id);
}
