package com.ftnam.image_ai_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ID_NOT_EXISTED(1001, "Id not existed", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1002, "User not existed", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
