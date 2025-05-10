package com.ftnam.image_ai_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    String language;

    @Enumerated(EnumType.STRING)
    @JsonProperty("subscription_plan")
    SubscriptionPlan subscriptionPlan;
}
