package com.ftnam.image_ai_backend.controller;

import com.ftnam.image_ai_backend.dto.request.HistoryRequest;
import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import com.ftnam.image_ai_backend.dto.response.HistoryResponse;
import com.ftnam.image_ai_backend.service.impl.HistoryServiceImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/history")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class HistoryController {
    HistoryServiceImpl historyService;

    @PostMapping
    ApiResponse<HistoryResponse> createHistory(@RequestBody HistoryRequest request){
        return ApiResponse.<HistoryResponse>builder()
                .message("Create history has been successfully")
                .value(historyService.createHistory(request))
                .build();
    }

    @GetMapping("/userId/{userId}")
    ApiResponse<List<HistoryResponse>> getHistoryByUser(@PathVariable String userId){
        return ApiResponse.<List<HistoryResponse>>builder()
                .value(historyService.getHistoryByUser(userId))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> deleteHistory(@PathVariable String id){
        historyService.deleteHistory(id);
        return ApiResponse.<Void>builder()
                .message("Delete history has been successfully")
                .build();
    }

    @DeleteMapping("/userId/{userId}")
    ApiResponse<Void> deleteHistoryByUser(@PathVariable String userId){
        historyService.deleteHistoryByUser(userId);
        return ApiResponse.<Void>builder()
                .message("Successfully cleared all user history")
                .build();
    }
}
