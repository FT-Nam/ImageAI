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
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "history_id")
    String id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "image_url")
    private String imageUrl;

    private String description;

    private String result;

    private double confident;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}
