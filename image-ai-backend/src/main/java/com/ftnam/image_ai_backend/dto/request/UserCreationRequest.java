package com.ftnam.image_ai_backend.dto.request;

import com.ftnam.image_ai_backend.entity.ImageHistory;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreationRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 10)
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,3})?[- .]?\\d{10,15}$")
    private String phone;
}
