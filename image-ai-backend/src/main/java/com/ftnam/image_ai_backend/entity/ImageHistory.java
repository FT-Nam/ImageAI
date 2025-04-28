package com.ftnam.image_ai_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ImageHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url")
    private String imageUrl;

    private String result;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
