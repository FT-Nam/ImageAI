package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.PlanInfoRequest;
import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;
import com.ftnam.image_ai_backend.entity.PlanInfo;
import com.ftnam.image_ai_backend.mapper.PlanInfoMapper;
import com.ftnam.image_ai_backend.repository.PlanInfoRepository;
import com.ftnam.image_ai_backend.service.PlanInfoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PlanInfoServiceImpl implements PlanInfoService {
    PlanInfoMapper planInfoMapper;
    PlanInfoRepository planInfoRepository;

    @Override
    public PlanInfoResponse createPlan(PlanInfoRequest request) {
        PlanInfo planInfo = planInfoMapper.toPlanInfo(request);
        return planInfoMapper.toPlanInfoResponse(planInfoRepository.save(planInfo));
    }

    @Override
    public List<PlanInfoResponse> getAllPlanInfo() {
        return planInfoRepository.findAll().stream().map(planInfoMapper::toPlanInfoResponse).toList();
    }
}
