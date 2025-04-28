package com.ftnam.image_ai_backend.exception;

import com.ftnam.image_ai_backend.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalException {
    @ExceptionHandler(exception = AppException.class)
    public ResponseEntity<ApiResponse> handlingAppException(AppException exception){
        ApiResponse apiResponse = ApiResponse.builder()
                .code(exception.getErrorCode().getCode())
                .message(exception.getErrorCode().getMessage())
                .build();

        return ResponseEntity.status(exception.getErrorCode().getHttpStatus()).body(apiResponse);
    }
}
