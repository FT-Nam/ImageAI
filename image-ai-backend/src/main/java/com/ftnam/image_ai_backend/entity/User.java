package com.ftnam.image_ai_backend.entity;

import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    String Id;

    private String name;

    @Email
    @Column(name = "email", unique = true)
    private String email;

    private String password;

    @Column(name = "phone", length = 15, unique = true)
    @Pattern(regexp = "^(\\+\\d{1,3})?[- .]?\\d{10,15}$")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription")
    private SubscriptionPlan subscription;

    private int credit;

    @Column(name = "credit_reset_at")
    private LocalDateTime creditResetAt;

    @Column(name = "subscription_expired_at")
    private LocalDateTime subscriptionExpiredAt;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<History> histories;

    @ManyToMany
    Set<Role> roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Order> orders;
}
