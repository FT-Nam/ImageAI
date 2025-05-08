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
    private String id;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("image_url")
    private String imageUrl;

    private String result;

    private String description;

    private double confident;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
