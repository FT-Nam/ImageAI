package com.ftnam.image_ai_backend.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanInfoResponse {
    @Enumerated(EnumType.STRING)
    private com.ftnam.image_ai_backend.enums.SubscriptionPlan subscription;

    private int weeklyCredit;

    private int price;
}
