package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;

import java.util.List;

public interface PlanInfoService {
    List<PlanInfoResponse> getAllPlanInfo();
}
