package com.ftnam.image_ai_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeResponse {
    private String prediction;

    private String description;

    private double accuracy;

    @JsonProperty("image_url")
    private String imageUrl;
}
