package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
