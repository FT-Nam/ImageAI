package com.ftnam.image_ai_backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

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
    @Size(min = 7)
    private String password;

    @Pattern(regexp = "^(\\+\\d{1,3})?[- .]?\\d{10,15}$")
    private String phone;
}
