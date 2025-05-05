package com.ftnam.image_ai_backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentReturnResponse {
    private boolean success;
    private String message;
    private String transactionCode;
    private String responseCode;
}
