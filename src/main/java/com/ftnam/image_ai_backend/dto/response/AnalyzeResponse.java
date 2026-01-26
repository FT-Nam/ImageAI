package com.ftnam.image_ai_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnalyzeResponse {
    String animal;
    double animal_confidence;
    String breed;
    double breed_confidence;
    String status;

    @JsonProperty("image_url")
    private String imageUrl;
}
