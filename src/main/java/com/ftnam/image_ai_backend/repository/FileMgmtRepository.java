package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.FileMgmt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileMgmtRepository extends JpaRepository<FileMgmt, String> {
}
