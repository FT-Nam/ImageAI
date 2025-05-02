package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
