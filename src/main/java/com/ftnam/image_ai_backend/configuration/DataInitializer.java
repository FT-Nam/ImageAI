package com.ftnam.image_ai_backend.configuration;

import com.ftnam.image_ai_backend.entity.PlanInfo;
import com.ftnam.image_ai_backend.entity.Role;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import com.ftnam.image_ai_backend.repository.PlanInfoRepository;
import com.ftnam.image_ai_backend.repository.RoleRepository;
import com.ftnam.image_ai_backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataInitializer implements CommandLineRunner {
    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    PlanInfoRepository planInfoRepository;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Seeding initial data...");
            seedAdminUser();
            log.info("✅ Default admin user created: admin@gmail.com / 12345678");
        } else {
            log.info("Database already initialized. Skipping seeding.");
        }
    }

    private void seedAdminUser(){
        Role adminRole = Role.builder()
                .name("ADMIN")
                .description("Administrator role")
                .build();

        Role userRole = Role.builder()
                .name("USER")
                .description("User role")
                .build();

        PlanInfo freePlan = PlanInfo.builder()
                .subscription(SubscriptionPlan.FREE)
                .price(0)
                .weeklyCredit(50)
                .build();

        User admin = User.builder()
                .name("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("12345678"))
                .phone("0989989989")
                .subscription(SubscriptionPlan.FREE)
                .credit(1000000000)
                .roles((Set.of(userRole)))
                .build();

        roleRepository.saveAll(List.of(adminRole,userRole));
        userRepository.save(admin);
        planInfoRepository.save(freePlan);
    }
}
