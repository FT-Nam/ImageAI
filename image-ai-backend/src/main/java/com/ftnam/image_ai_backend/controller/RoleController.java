package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.request.RoleRequest;
import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.RoleResponse;
import com.ftnam.image_ai_backend.service.impl.RoleServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/role")
public class RoleController {
    RoleServiceImpl roleService;

    @PostMapping
    ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest request){
        return ApiResponse.<RoleResponse>builder()
                .value(roleService.createRole(request))
                .message("Create role has been successfully")
                .build();
    }

    @GetMapping
    ApiResponse<List<RoleResponse>> getAll(){
        return ApiResponse.<List<RoleResponse>>builder()
                .value(roleService.getAll())
                .build();
    }

    @DeleteMapping("/{name}")
    ApiResponse<Void> delete(@PathVariable String name){
        roleService.delete(name);
        return ApiResponse.<Void>builder()
                .message("Delete role has been successfully")
                .build();
    }
}
