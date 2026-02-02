package com.ftnam.image_ai_backend.scheduler;

import com.ftnam.image_ai_backend.dto.event.EmailEvent;
import com.ftnam.image_ai_backend.entity.PlanInfo;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.PlanInfoRepository;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.util.NotificationSender;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreditResetScheduler {
    UserRepository userRepository;
    PlanInfoRepository planInfoRepository;
    NotificationSender notificationSender;

    KafkaTemplate<String,Object> kafkaTemplate;

    // Run in 00:00
    @Scheduled(cron = "0 0 0 * * *")
    public void resetWeeklyCredit(){
        List<User> users = userRepository.findAll();

        for(User user : users){
            boolean changed = false;
            LocalDateTime now = LocalDateTime.now();

            if(user.getSubscriptionExpiredAt() != null && now.isAfter(user.getSubscriptionExpiredAt())){
                user.setSubscriptionExpiredAt(null);
                user.setSubscription(SubscriptionPlan.FREE);
                user.setCreditResetAt(LocalDateTime.now());
                changed = true;

                EmailEvent emailEvent = EmailEvent.builder()
                        .channel("EMAIL")
                        .recipient(user.getEmail())
                        .templateId(3)
                        .params(Map.of(
                                "subscription", user.getSubscription().toString(),
                                "name", user.getName()
                        ))
                        .build();


                kafkaTemplate.send("email-delivery", emailEvent);

                notificationSender.sendNotification(user.getId(), "Your subscription plan has expired, Please renew to continue using.");

                log.info("Subscription plan of user {} expired,reset free plan", user.getEmail());
            }

            boolean weeklyReset = Duration.between(user.getCreditResetAt(), now).toDays() >= 7;

            if (weeklyReset){
                PlanInfo planInfo = planInfoRepository.findBySubscription(user.getSubscription())
                        .orElseThrow(()-> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

                int newCredit = Math.min((user.getCredit() + planInfo.getWeeklyCredit()), 5000);

                user.setCredit(newCredit);
                user.setCreditResetAt(LocalDateTime.now());
                changed = true;

                notificationSender.sendNotification(user.getId(), "You have received " + planInfo.getWeeklyCredit() +  " credits. Start analyzing your images!");

                log.info("Reset credit of user {} sccessfully", user.getEmail());
            }

            if (changed) {
                userRepository.save(user);
            }
        }
    }
}
