package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.HistoryResponse;

import java.util.List;

public interface HistoryService {
    HistoryResponse createHistory(HistoryRequest request);

    List<HistoryResponse> getHistoryByUser(Long userId);

    void deleteHistory(Long id);

    void deleteHistoryByUser(Long userId);
}
