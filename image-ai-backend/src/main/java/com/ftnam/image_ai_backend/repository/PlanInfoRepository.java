package com.ftnam.image_ai_backend.repository;

import com.ftnam.image_ai_backend.entity.PlanInfo;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanInfoRepository extends JpaRepository<PlanInfo, String> {
    Optional<PlanInfo> findBySubscription(SubscriptionPlan subscriptionPlan);
}
