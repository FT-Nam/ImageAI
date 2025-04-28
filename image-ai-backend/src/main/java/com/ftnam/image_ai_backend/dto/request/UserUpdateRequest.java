package com.ftnam.image_ai_backend.dto.request;

import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UserUpdateRequest {
    private String name;

    private String email;

    @Size(min = 10)
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,3})?[- .]?\\d{10,15}$")
    private String phone;
}
