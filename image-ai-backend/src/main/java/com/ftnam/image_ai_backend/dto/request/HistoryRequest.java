package com.ftnam.image_ai_backend.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryRequest {
    @NotNull
    @JsonProperty("user_id")
    private String userId;

    @NotBlank
    @JsonProperty("image_url")
    private String imageUrl;

    @NotBlank
    private String description;

    @NotBlank
    private String result;

    @NotNull
    private double confident;

}
