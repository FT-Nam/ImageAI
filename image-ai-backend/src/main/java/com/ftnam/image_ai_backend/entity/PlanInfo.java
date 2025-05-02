package com.ftnam.image_ai_backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plan_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_info_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription")
    private com.ftnam.image_ai_backend.enums.SubscriptionPlan subscription;

    @Column(name = "weekly_credit")
    private int weeklyCredit;

    private int price;
}
