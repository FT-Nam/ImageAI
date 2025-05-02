package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.request.UserCreationRequest;
import com.ftnam.image_ai_backend.dto.request.UserUpdateRequest;
import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.PaginationInfo;
import com.ftnam.image_ai_backend.dto.response.UserResponse;
import com.ftnam.image_ai_backend.service.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    UserServiceImpl userService;

    @PostMapping
    ApiResponse<UserResponse> createUser(@RequestBody @Valid UserCreationRequest request){
        return ApiResponse.<UserResponse>builder()
                .message("Create user has been successfully")
                .value(userService.createUser(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<UserResponse>> getUsers(Pageable pageable){
        Page<UserResponse> userResponses = userService.getUsers(pageable);
        return ApiResponse.<List<UserResponse>>builder()
                .value(userResponses.getContent())
                .paginationInfo(PaginationInfo.builder()
                        .page(userResponses.getNumber())
                        .size(userResponses.getSize())
                        .totalElements(userResponses.getTotalElements())
                        .build())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getUserById(@PathVariable String id){
        return ApiResponse.<UserResponse>builder()
                .value(userService.getUserById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<UserResponse> updateUser(@PathVariable String id, @RequestBody @Valid UserUpdateRequest request){
        return ApiResponse.<UserResponse>builder()
                .message("Update user has been successfully")
                .value(userService.updateUser(id,request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<UserResponse> deleteUser(@PathVariable String id){
        userService.deleteUser(id);
        return ApiResponse.<UserResponse>builder()
                .message("Delete user has been successfully")
                .build();
    }
}
