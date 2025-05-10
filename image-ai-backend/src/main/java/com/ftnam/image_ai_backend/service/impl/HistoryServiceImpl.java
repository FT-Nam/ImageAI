package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.HistoryResponse;
import com.ftnam.image_ai_backend.entity.History;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.mapper.HistoryMapper;
import com.ftnam.image_ai_backend.repository.HistoryRepository;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.HistoryService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {
    HistoryRepository historyRepository;
    HistoryMapper historyMapper;
    UserRepository userRepository;

    @Override
    public HistoryResponse createHistory(HistoryRequest request) {
        History history = historyMapper.toHistory(request);
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
        history.setUser(user);
        return historyMapper.toHistoryResponse(historyRepository.save(history));
    }

    @Override
    public List<HistoryResponse> getHistoryByUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return user.getHistories().stream().map(historyMapper::toHistoryResponse).toList();
    }

    @Override
    public void deleteHistory(String id) {
        historyRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteHistoryByUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));
        historyRepository.deleteByUser(user);
    }
}
