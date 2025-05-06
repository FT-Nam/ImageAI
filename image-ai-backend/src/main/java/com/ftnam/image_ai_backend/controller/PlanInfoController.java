package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.request.PlanInfoRequest;
import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;
import com.ftnam.image_ai_backend.service.impl.PlanInfoServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/plan")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlanInfoController {
    PlanInfoServiceImpl planInfoService;

    @PostMapping
    public ApiResponse<PlanInfoResponse> createPlan(@RequestBody PlanInfoRequest request){
        return ApiResponse.<PlanInfoResponse>builder()
                .value(planInfoService.createPlan(request))
                .message("Create plan has been successfully")
                .build();
    }

    @GetMapping
    public ApiResponse<List<PlanInfoResponse>> getPlans(){
        return ApiResponse.<List<PlanInfoResponse>>builder()
                .value(planInfoService.getAllPlanInfo())
                .build();
    }
}
