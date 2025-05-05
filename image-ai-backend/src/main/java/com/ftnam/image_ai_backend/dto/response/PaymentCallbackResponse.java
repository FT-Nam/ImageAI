package com.ftnam.image_ai_backend.dto.response;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentCallbackResponse {
    private String rspCode;
    private String message;
}
