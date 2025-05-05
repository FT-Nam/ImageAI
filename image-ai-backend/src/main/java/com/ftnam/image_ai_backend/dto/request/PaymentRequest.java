package com.ftnam.image_ai_backend.dto.request;

import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentRequest {
    long amount;
    String bankCode;
    String ipAddress;
    String language;
    SubscriptionPlan subscriptionPlan;
}
