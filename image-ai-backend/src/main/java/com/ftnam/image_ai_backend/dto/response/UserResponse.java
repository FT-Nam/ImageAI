package com.ftnam.image_ai_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String name;

    private String email;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan subscription;

    private int credit;

    private String phone;

    @JsonProperty("credit_reset_at")
    private LocalDateTime creditResetAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
