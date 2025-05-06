package com.ftnam.image_ai_backend.scheduler;

import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import com.ftnam.image_ai_backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreditResetScheduler {
    UserRepository userRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void resetWeeklyCredit(){
        List<User> users = userRepository.findAll();

        for(User user : users){
            LocalDateTime now = LocalDateTime.now();

            if(user.getSubscriptionExpiredAt() != null && now.isAfter(user.getSubscriptionExpiredAt())){
                user.setSubscriptionExpiredAt(null);
                user.setSubscription(SubscriptionPlan.FREE);
                userRepository.save(user);
                log.info("Subscription plan of user {} expired,reset free plan", user.getEmail());
            }

            boolean weeklyReset = Duration.between(user.getCreditResetAt(), now).toDays() >= 7;

            if (weeklyReset){
                int creditSet = switch (user.getSubscription()){
                    case PREMIUM -> 500;
                    case PRO -> 1000;
                    default -> 100;
                };

                int newCredit = Math.min((user.getCredit() + creditSet), 5000);

                user.setCredit(newCredit);
                user.setCreditResetAt(LocalDateTime.now());
                userRepository.save(user);
                log.info("Reset credit of user {} sccessfully", user.getEmail());
            }
        }
    }
}
