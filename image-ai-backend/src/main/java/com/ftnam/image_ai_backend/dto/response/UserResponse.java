package com.ftnam.image_ai_backend.dto.response;

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

    private LocalDateTime creditResetAt;

    private LocalDateTime createdAt;
}
