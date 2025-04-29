package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;
import com.ftnam.image_ai_backend.entity.PlanInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanInfoMapper {
    PlanInfoResponse toPlanInfoResponse(PlanInfo planInfo);
}
