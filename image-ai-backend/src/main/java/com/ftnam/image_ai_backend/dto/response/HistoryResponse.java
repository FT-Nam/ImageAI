package com.ftnam.image_ai_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryResponse {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("image_url")
    private String imageUrl;

    private String result;

    private float confident;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
