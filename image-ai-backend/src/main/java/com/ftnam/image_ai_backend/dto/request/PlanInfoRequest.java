package com.ftnam.image_ai_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.Column;
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
public class PlanInfoRequest {
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscription;

    @JsonProperty("weekly_credit")
    private int weeklyCredit;

    private int price;
}
