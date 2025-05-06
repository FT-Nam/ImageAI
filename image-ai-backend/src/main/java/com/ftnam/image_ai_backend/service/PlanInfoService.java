package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.PlanInfoRequest;
import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;

import java.util.List;

public interface PlanInfoService {
    PlanInfoResponse createPlan(PlanInfoRequest request);
    List<PlanInfoResponse> getAllPlanInfo();
}
