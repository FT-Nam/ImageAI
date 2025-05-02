package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.History;
import com.ftnam.image_ai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History,String> {
    void deleteByUser(User user);
}
