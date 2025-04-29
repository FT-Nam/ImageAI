package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;
import com.ftnam.image_ai_backend.service.impl.PlanInfoServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plans")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlanInfoController {
    PlanInfoServiceImpl planInfoService;
    @GetMapping
    public ApiResponse<List<PlanInfoResponse>> getPlans(){
        return ApiResponse.<List<PlanInfoResponse>>builder()
                .value(planInfoService.getAllPlanInfo())
                .build();
    }
}
