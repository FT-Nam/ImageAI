package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.ImageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageHistoryRepository extends JpaRepository<ImageHistory,Long> {
}
