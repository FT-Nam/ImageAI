package com.ftnam.image_ai_backend.entity;

import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subscription_plan_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubscriptionPlanInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription")
    private SubscriptionPlan subscription;

    @Column(name = "weekly_credit")
    private int weeklyCredit;

    private int price;
}
