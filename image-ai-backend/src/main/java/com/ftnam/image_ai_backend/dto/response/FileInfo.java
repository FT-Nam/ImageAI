package com.ftnam.image_ai_backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileInfo {
    String name;

    @JsonProperty("content_type")
    String contentType;

    long size;

    @JsonProperty("md5_checksum")
    String md5Checksum;

    String path;

    String url;
}
