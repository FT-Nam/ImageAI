package com.ftnam.image_ai_backend.mapper;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.HistoryResponse;
import com.ftnam.image_ai_backend.entity.History;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HistoryMapper {
    History toHistory(HistoryRequest request);

    @Mapping(source = "user.id", target = "userId")
    HistoryResponse toHistoryResponse(History history);
}
