package com.ftnam.image_ai_backend.entity;

import com.ftnam.image_ai_backend.enums.OrderStatus;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "order")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    String code;

    @ManyToOne
    User user;

    @Column(name = "subscription_plan")
    SubscriptionPlan subscriptionPlan;

    Long amount;

    OrderStatus status;

    @Column(name = "created_at")
    LocalDateTime createdAt;
}
