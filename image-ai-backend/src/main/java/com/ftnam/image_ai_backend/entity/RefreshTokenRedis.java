package com.ftnam.image_ai_backend.entity;

import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@RedisHash("refresh_token")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshTokenRedis implements Serializable {
    @Id
    String id;

    String token;

    @TimeToLive
    Long expirationTime;
}
