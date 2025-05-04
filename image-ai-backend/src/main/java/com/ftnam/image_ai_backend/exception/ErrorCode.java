package com.ftnam.image_ai_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    ID_NOT_EXISTED(1001, "Id not existed", HttpStatus.NOT_FOUND),
    USER_NOT_EXISTED(1002, "User not existed", HttpStatus.NOT_FOUND),
    INVALID_FILE_TYPE(1003, "Invalid file type. Only image files are allowed", HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND(1004, "File not found", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1005, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    USER_EXISTED(1006, "User existed", HttpStatus.BAD_REQUEST),
    ROLE_EXISTED(1007, "Role existed", HttpStatus.BAD_REQUEST),
    PERMISSION_EXISTED(1008, "Permission existed", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
