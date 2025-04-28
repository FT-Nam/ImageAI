package com.ftnam.image_ai_backend.exception;

import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppException extends RuntimeException {
    ErrorCode errorCode;
}
