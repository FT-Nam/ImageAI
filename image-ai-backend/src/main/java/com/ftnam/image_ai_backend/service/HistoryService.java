package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.HistoryResponse;

import java.util.List;

public interface HistoryService {
    HistoryResponse createHistory(HistoryRequest request);

    List<HistoryResponse> getHistoryByUser(String userId);

    void deleteHistory(String id);

    void deleteHistoryByUser(String userId);
}
