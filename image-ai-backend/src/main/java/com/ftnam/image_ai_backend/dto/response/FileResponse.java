package com.ftnam.image_ai_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponse {
    @JsonProperty("original_file_name")
    String originalFileName;
    String url;
}
