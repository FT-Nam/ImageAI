package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.request.PlanInfoRequest;
import com.ftnam.image_ai_backend.dto.response.PlanInfoResponse;
import com.ftnam.image_ai_backend.entity.PlanInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlanInfoMapper {
    PlanInfo toPlanInfo(PlanInfoRequest request);

    PlanInfoResponse toPlanInfoResponse(PlanInfo planInfo);
}
