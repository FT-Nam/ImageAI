package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.RefreshTokenRedis;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRedisRepository extends CrudRepository<RefreshTokenRedis, String> {
}
