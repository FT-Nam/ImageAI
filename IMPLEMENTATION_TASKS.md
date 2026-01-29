# 🚀 IMPLEMENTATION TASKS - Plan & Credit System Refactor

**Mục tiêu**: Triển khai spec Plan & Credit Policy mới theo đúng yêu cầu SaaS production-ready.

**Thời gian ước tính**: ~46 giờ (~6 ngày dev)

---

## 📋 PHASE 1: DATABASE & ENTITY REFACTOR (Priority: HIGH)

### Task 1.1: Update SubscriptionPlan Enum
**File**: `src/main/java/com/ftnam/image_ai_backend/enums/SubscriptionPlan.java`

**Action**:
```java
public enum SubscriptionPlan {
    FREE, BASIC, PRO  // Đổi PREMIUM → BASIC
}
```

**Checklist**:
- [ ] Đổi `PREMIUM` → `BASIC`
- [ ] Tìm tất cả chỗ dùng `PREMIUM` trong code → đổi sang `BASIC`
- [ ] Update database enum nếu cần migration

**Files cần search & replace**:
- `PaymentServiceImpl.java`
- `CreditResetScheduler.java`
- `PlanInfo.java`
- `User.java` (nếu có default)
- Frontend: `Plan.js`, `Plans.js`

---

### Task 1.2: Update PlanInfo Entity
**File**: `src/main/java/com/ftnam/image_ai_backend/entity/PlanInfo.java`

**Action**:
```java
@Entity
@Table(name = "subscription_plan_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlanInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "plan_info_id")
    String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription")
    private SubscriptionPlan subscription;

    @Column(name = "monthly_credit")  // ĐỔI TÊN: weeklyCredit → monthlyCredit
    private int monthlyCredit;

    @Column(name = "max_credit")  // THÊM MỚI
    private int maxCredit;

    private int price;
}
```

**Checklist**:
- [ ] Đổi `weeklyCredit` → `monthlyCredit`
- [ ] Thêm field `maxCredit`
- [ ] Update `PlanInfoMapper` nếu cần
- [ ] Update `PlanInfoRequest` DTO
- [ ] Update `PlanInfoResponse` DTO

**Migration SQL** (chạy trên DB):
```sql
ALTER TABLE subscription_plan_info 
  CHANGE COLUMN weekly_credit monthly_credit INT NOT NULL,
  ADD COLUMN max_credit INT NOT NULL DEFAULT 50 AFTER monthly_credit;

-- Update data mẫu
UPDATE subscription_plan_info SET monthly_credit = 50, max_credit = 50 WHERE subscription = 'FREE';
UPDATE subscription_plan_info SET monthly_credit = 600, max_credit = 800 WHERE subscription = 'BASIC';
UPDATE subscription_plan_info SET monthly_credit = 2000, max_credit = 2500 WHERE subscription = 'PRO';
```

---

### Task 1.3: Update User Entity - Add Missing Fields
**File**: `src/main/java/com/ftnam/image_ai_backend/entity/User.java`

**Action**:
```java
@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    // ... existing fields ...

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription")
    private SubscriptionPlan subscription;

    private int credit;

    @Column(name = "credit_reset_at")
    private LocalDateTime creditResetAt;

    @Column(name = "subscription_expired_at")
    private LocalDateTime subscriptionExpiredAt;

    @Column(name = "subscription_locked_until")  // THÊM MỚI
    private LocalDateTime subscriptionLockedUntil;

    @Column(name = "payment_grace_until")  // THÊM MỚI
    private LocalDateTime paymentGraceUntil;

    @Version  // THÊM MỚI - Optimistic Locking
    private Long version;

    // ... rest of fields ...
}
```

**Checklist**:
- [ ] Thêm `subscriptionLockedUntil`
- [ ] Thêm `paymentGraceUntil`
- [ ] Thêm `@Version` cho optimistic locking
- [ ] Update `UserResponse` DTO nếu cần

**Migration SQL**:
```sql
ALTER TABLE user 
  ADD COLUMN subscription_locked_until DATETIME NULL AFTER subscription_expired_at,
  ADD COLUMN payment_grace_until DATETIME NULL AFTER subscription_locked_until,
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER payment_grace_until;
```

---

## 📋 PHASE 2: CREDIT COST LOGIC (Priority: HIGH)

### Task 2.1: Create CreditCostCalculator Service
**File**: `src/main/java/com/ftnam/image_ai_backend/service/CreditCostCalculator.java` (NEW)

**Action**:
```java
package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.response.AnalyzeResponse;
import org.springframework.stereotype.Component;

@Component
public class CreditCostCalculator {
    
    /**
     * Tính credit cost dựa trên kết quả analyze
     * - DOG/CAT/OTHER chỉ: 1 credit
     * - DOG + breed: 2 credits
     * - CAT + breed: 2 credits
     */
    public int calculateCost(AnalyzeResponse response) {
        String animal = response.getAnimal();
        String breed = response.getBreed();
        
        // Base cost: 1 credit cho mọi request
        int cost = 1;
        
        // Nếu có breed và không phải OTHER → +1 credit
        if (breed != null && !breed.isEmpty() && !breed.equalsIgnoreCase("OTHER")) {
            if ("DOG".equalsIgnoreCase(animal) || "CAT".equalsIgnoreCase(animal)) {
                cost += 1; // Total = 2
            }
        }
        
        return cost;
    }
}
```

**Checklist**:
- [ ] Tạo file mới
- [ ] Implement logic tính cost
- [ ] Unit test: test các case (DOG only, DOG+breed, CAT+breed, OTHER)

**Test cases**:
```java
// Test: DOG only → 1 credit
// Test: DOG + breed → 2 credits
// Test: CAT only → 1 credit
// Test: CAT + breed → 2 credits
// Test: OTHER → 1 credit
```

---

### Task 2.2: Update AnalyzeServiceImpl - Use Dynamic Credit Cost
**File**: `src/main/java/com/ftnam/image_ai_backend/service/impl/AnalyzeServiceImpl.java`

**Action**:
```java
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyzeServiceImpl implements AnalyzeService {
    // ... existing fields ...
    CreditCostCalculator creditCostCalculator;  // THÊM MỚI

    // XÓA: @Value("${app.analyze.credit-cost}") int creditCost;

    @Override
    @Transactional
    public AnalyzeResponse analyzeImage(MultipartFile file) throws IOException {
        // ... existing auth check ...

        // Upload file
        var upload = fileService.uploadFile(file);
        String uploadedFileId = upload.getFileId();
        
        // Call Python service
        AnalyzeResponse predict;
        try {
            predict = pythonServiceClient.predict(file);
        } catch (Exception e) {
            // ... existing error handling ...
        }

        // TÍNH COST ĐỘNG - SAU KHI CÓ KẾT QUẢ
        int creditCost = creditCostCalculator.calculateCost(predict);

        if(user != null){
            // Check credit với cost đã tính
            if(user.getCredit() < creditCost){
                notificationPublisher.sendNotification(userId, 
                    "Insufficient credits. Required: " + creditCost);
                throw new AppException(ErrorCode.NOT_ENOUGH_CREDITS);
            }

            // Deduct credit
            user.setCredit(user.getCredit() - creditCost);
            userRepository.save(user);

            // ... rest of logic ...
        }

        return predict;
    }
}
```

**Checklist**:
- [ ] Inject `CreditCostCalculator`
- [ ] Xóa `@Value("${app.analyze.credit-cost}")`
- [ ] Tính cost SAU khi có kết quả từ Python
- [ ] Update check credit với cost động
- [ ] Update notification message để show cost

**Lưu ý**: Cost phải tính SAU khi có kết quả từ Python, không phải trước!

---

## 📋 PHASE 3: CREDIT RESET LOGIC (Priority: HIGH)

### Task 3.1: Create CreditService - Lazy Reset Logic
**File**: `src/main/java/com/ftnam/image_ai_backend/service/CreditService.java` (NEW)

**Action**:
```java
package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.entity.PlanInfo;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.repository.PlanInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {
    private final PlanInfoRepository planInfoRepository;

    /**
     * Lazy credit reset - gọi trước mọi action tốn credit
     * Logic: Nếu đã qua 30 ngày → reset monthly credit với hard cap
     */
    @Transactional
    public void ensureCreditValid(User user) {
        if (user.getCreditResetAt() == null) {
            user.setCreditResetAt(LocalDateTime.now());
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysSinceReset = Duration.between(user.getCreditResetAt(), now).toDays();

        // Monthly reset: 30 ngày
        if (daysSinceReset >= 30) {
            PlanInfo planInfo = planInfoRepository.findBySubscription(user.getSubscription())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));

            // Hard cap rollover: min(current + monthly, maxCredit)
            int newCredit = Math.min(
                    user.getCredit() + planInfo.getMonthlyCredit(),
                    planInfo.getMaxCredit()
            );

            user.setCredit(newCredit);
            user.setCreditResetAt(now);
            
            log.info("Lazy reset credit for user {}: {} credits (cap: {})", 
                    user.getEmail(), newCredit, planInfo.getMaxCredit());
        }
    }
}
```

**Checklist**:
- [ ] Tạo file mới `CreditService.java`
- [ ] Implement `ensureCreditValid()` với logic monthly + hard cap
- [ ] Unit test: test reset sau 30 ngày, test hard cap

---

### Task 3.2: Update CreditResetScheduler - Monthly Reset
**File**: `src/main/java/com/ftnam/image_ai_backend/scheduler/CreditResetScheduler.java`

**Action**:
```java
@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CreditResetScheduler {
    UserRepository userRepository;
    PlanInfoRepository planInfoRepository;
    NotificationPublisher notificationPublisher;
    KafkaTemplate<String,Object> kafkaTemplate;

    // Backup cron - chạy hàng ngày
    @Scheduled(cron = "0 0 0 * * *")
    public void resetWeeklyCredit(){
        List<User> users = userRepository.findAll();

        for(User user : users){
            boolean changed = false;
            LocalDateTime now = LocalDateTime.now();

            // Check subscription expired
            if(user.getSubscriptionExpiredAt() != null && now.isAfter(user.getSubscriptionExpiredAt())){
                // DOWNGRADE LOGIC với grace rule
                if(user.getCredit() > 50){
                    // Grace: giữ nguyên credit, chỉ đổi plan
                    user.setSubscription(SubscriptionPlan.FREE);
                    log.info("Downgrade user {} to FREE, keep credit: {}", 
                            user.getEmail(), user.getCredit());
                } else {
                    // Reset về FREE credit
                    user.setCredit(50);
                    user.setSubscription(SubscriptionPlan.FREE);
                    log.info("Downgrade user {} to FREE, reset credit to 50", 
                            user.getEmail());
                }
                
                user.setSubscriptionExpiredAt(null);
                user.setCreditResetAt(LocalDateTime.now());
                changed = true;

                // ... email & notification ...
            }

            // MONTHLY RESET: 30 ngày (thay vì 7)
            boolean monthlyReset = Duration.between(user.getCreditResetAt(), now).toDays() >= 30;

            if (monthlyReset){
                PlanInfo planInfo = planInfoRepository.findBySubscription(user.getSubscription())
                        .orElseThrow(()-> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

                // Hard cap theo plan (không phải fix cứng 5000)
                int maxCredit = planInfo.getMaxCredit();
                int newCredit = Math.min(
                        (user.getCredit() + planInfo.getMonthlyCredit()),
                        maxCredit
                );

                user.setCredit(newCredit);
                user.setCreditResetAt(LocalDateTime.now());
                changed = true;

                notificationPublisher.sendNotification(user.getId(),
                        "You have received " + planInfo.getMonthlyCredit() + 
                        " credits. Total: " + newCredit + " (max: " + maxCredit + ")");

                log.info("Monthly reset credit of user {}: {} credits", 
                        user.getEmail(), newCredit);
            }

            if (changed) {
                userRepository.save(user);
            }
        }
    }
}
```

**Checklist**:
- [ ] Đổi weekly (7 ngày) → monthly (30 ngày)
- [ ] Lấy `maxCredit` từ `PlanInfo` thay vì hard code 5000
- [ ] Thêm downgrade grace rule (giữ credit nếu > 50)
- [ ] Update notification message

---

### Task 3.3: Integrate Lazy Reset vào AnalyzeService
**File**: `src/main/java/com/ftnam/image_ai_backend/service/impl/AnalyzeServiceImpl.java`

**Action**:
```java
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AnalyzeServiceImpl implements AnalyzeService {
    // ... existing fields ...
    CreditService creditService;  // THÊM MỚI

    @Override
    @Transactional
    public AnalyzeResponse analyzeImage(MultipartFile file) throws IOException {
        // ... auth check ...

        if(user != null){
            // LAZY RESET - gọi trước khi check credit
            creditService.ensureCreditValid(user);
            
            // Reload user để có credit mới nhất
            user = userRepository.findById(userId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

            // ... rest of logic ...
        }
    }
}
```

**Checklist**:
- [ ] Inject `CreditService`
- [ ] Gọi `ensureCreditValid()` TRƯỚC khi check credit
- [ ] Reload user sau khi reset (hoặc dùng entity manager refresh)

---

## 📋 PHASE 4: OPTIMISTIC LOCKING (Priority: HIGH)

### Task 4.1: Create CreditDeductionService với Retry
**File**: `src/main/java/com/ftnam/image_ai_backend/service/CreditDeductionService.java` (NEW)

**Action**:
```java
package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditDeductionService {
    private final UserRepository userRepository;

    /**
     * Deduct credit với optimistic locking + retry
     */
    @Transactional
    @Retryable(
        value = {org.hibernate.StaleObjectStateException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void deductCredit(String userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getCredit() < amount) {
            throw new AppException(ErrorCode.NOT_ENOUGH_CREDITS);
        }

        user.setCredit(user.getCredit() - amount);
        userRepository.save(user);
        
        log.debug("Deducted {} credits from user {}. Remaining: {}", 
                amount, userId, user.getCredit());
    }
}
```

**Checklist**:
- [ ] Tạo file mới
- [ ] Add dependency `spring-retry` vào `pom.xml`
- [ ] Enable retry: `@EnableRetry` trong `@SpringBootApplication`
- [ ] Implement retry logic với `@Retryable`
- [ ] Unit test: test concurrent deduction

**pom.xml**:
```xml
<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>
```

**ImageAiBackendApplication.java**:
```java
@SpringBootApplication
@EnableRetry  // THÊM
public class ImageAiBackendApplication { ... }
```

---

### Task 4.2: Update AnalyzeServiceImpl - Use CreditDeductionService
**File**: `src/main/java/com/ftnam/image_ai_backend/service/impl/AnalyzeServiceImpl.java`

**Action**:
```java
@Service
public class AnalyzeServiceImpl implements AnalyzeService {
    // ... existing fields ...
    CreditDeductionService creditDeductionService;  // THÊM MỚI

    @Override
    @Transactional
    public AnalyzeResponse analyzeImage(MultipartFile file) throws IOException {
        // ... existing logic ...

        if(user != null){
            creditService.ensureCreditValid(user);
            user = userRepository.findById(userId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

            int creditCost = creditCostCalculator.calculateCost(predict);

            // DÙNG SERVICE MỚI - có retry
            creditDeductionService.deductCredit(userId, creditCost);

            // ... rest of logic ...
        }
    }
}
```

**Checklist**:
- [ ] Inject `CreditDeductionService`
- [ ] Thay thế manual deduction bằng service call
- [ ] Xóa code cũ: `user.setCredit(...)` và `userRepository.save(user)`

---

## 📋 PHASE 5: UPGRADE/DOWNGRADE LOGIC (Priority: MEDIUM)

### Task 5.1: Update PaymentServiceImpl - Upgrade Logic
**File**: `src/main/java/com/ftnam/image_ai_backend/service/impl/PaymentServiceImpl.java`

**Action**:
```java
@Override
@Transactional
public PaymentReturnResponse paymentReturn(HttpServletRequest request) {
    // ... existing validation ...

    if ("00".equals(request.getParameter("vnp_ResponseCode"))) {
        User user = order.getUser();
        SubscriptionPlan plan = order.getSubscriptionPlan();

        PlanInfo planInfo = planInfoRepository.findBySubscription(plan)
                .orElseThrow(()-> new AppException(ErrorCode.SUBSCRIPTION_NOT_EXISTED));

        // UPGRADE LOGIC: cộng monthlyCredit với hard cap
        int newCredit = Math.min(
                user.getCredit() + planInfo.getMonthlyCredit(),
                planInfo.getMaxCredit()
        );

        user.setSubscription(plan);
        user.setCredit(newCredit);
        user.setSubscriptionExpiredAt(LocalDateTime.now().plusMonths(1));
        user.setCreditResetAt(LocalDateTime.now());
        
        // LOCK SUBSCRIPTION 30 NGÀY
        user.setSubscriptionLockedUntil(LocalDateTime.now().plusDays(30));

        order.setStatus(OrderStatus.SUCCESS);

        userRepository.save(user);
        orderRepository.save(order);

        // ... notification ...
    }
}
```

**Checklist**:
- [ ] Đổi `weeklyCredit` → `monthlyCredit`
- [ ] Thêm hard cap: `Math.min(current + monthly, maxCredit)`
- [ ] Set `subscriptionLockedUntil` = now + 30 days
- [ ] Update notification message

---

### Task 5.2: Add Downgrade Prevention Logic
**File**: `src/main/java/com/ftnam/image_ai_backend/service/impl/PaymentServiceImpl.java`

**Action**:
```java
@Override
public String createPayment(PaymentRequest request, HttpServletRequest httpServletRequest) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findById(userId)
            .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

    // CHECK LOCK PERIOD - không cho downgrade/refund trong lock
    if (user.getSubscriptionLockedUntil() != null 
            && LocalDateTime.now().isBefore(user.getSubscriptionLockedUntil())) {
        throw new AppException(ErrorCode.SUBSCRIPTION_LOCKED);
    }

    // ... rest of payment creation ...
}
```

**Checklist**:
- [ ] Thêm check `subscriptionLockedUntil` trước khi tạo payment
- [ ] Tạo `ErrorCode.SUBSCRIPTION_LOCKED` nếu chưa có
- [ ] Update frontend để show message khi locked

---

### Task 5.3: Payment Fail Grace Period
**File**: `src/main/java/com/ftnam/image_ai_backend/service/impl/PaymentServiceImpl.java`

**Action**:
```java
@Override
@Transactional
public PaymentReturnResponse paymentReturn(HttpServletRequest request) {
    // ... existing code ...

    if (!"00".equals(request.getParameter("vnp_ResponseCode"))) {
        // PAYMENT FAILED
        User user = order.getUser();
        order.setStatus(OrderStatus.FAILED);
        orderRepository.save(order);

        // GRACE PERIOD: 7 ngày
        user.setPaymentGraceUntil(LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        notificationPublisher.sendNotification(user.getId(),
                "Payment failed. You have 7 days grace period to retry payment.");

        return PaymentReturnResponse.builder()
                .success(false)
                .message("Transaction failed")
                .build();
    }
}
```

**Checklist**:
- [ ] Set `paymentGraceUntil` khi payment fail
- [ ] Schedule job để check grace period (sau 7 ngày → downgrade)
- [ ] Update scheduler để handle grace period expiry

---

### Task 5.4: Update Scheduler - Handle Grace Period Expiry
**File**: `src/main/java/com/ftnam/image_ai_backend/scheduler/CreditResetScheduler.java`

**Action**:
```java
@Scheduled(cron = "0 0 0 * * *")
public void resetWeeklyCredit(){
    List<User> users = userRepository.findAll();
    LocalDateTime now = LocalDateTime.now();

    for(User user : users){
        boolean changed = false;

        // CHECK PAYMENT GRACE PERIOD EXPIRY
        if (user.getPaymentGraceUntil() != null 
                && now.isAfter(user.getPaymentGraceUntil())) {
            // Grace period expired → downgrade to FREE
            if(user.getCredit() > 50){
                user.setSubscription(SubscriptionPlan.FREE);
            } else {
                user.setCredit(50);
                user.setSubscription(SubscriptionPlan.FREE);
            }
            user.setPaymentGraceUntil(null);
            changed = true;

            notificationPublisher.sendNotification(user.getId(),
                    "Payment grace period expired. Subscription downgraded to FREE.");
        }

        // ... rest of existing logic ...
    }
}
```

**Checklist**:
- [ ] Thêm check `paymentGraceUntil` expiry
- [ ] Downgrade khi grace period hết
- [ ] Clear `paymentGraceUntil` sau khi downgrade

---

## 📋 PHASE 6: RATE LIMITING (Priority: MEDIUM)

### Task 6.1: Add Rate Limiting Dependencies
**File**: `pom.xml`

**Action**:
```xml
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
<dependency>
    <groupId>com.github.vladimir-bukhtoyarov</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>8.7.0</version>
</dependency>
```

**Checklist**:
- [ ] Add dependencies
- [ ] Maven install để download

---

### Task 6.2: Create RateLimitConfig
**File**: `src/main/java/com/ftnam/image_ai_backend/configuration/RateLimitConfig.java` (NEW)

**Action**:
```java
package com.ftnam.image_ai_backend.configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitConfig {

    /**
     * Rate limits theo plan:
     * FREE: 5 req/min + 20 req/hour
     * BASIC: 20 req/min (burst 50)
     * PRO: 60 req/min (burst 200)
     */
    @Bean
    public Map<SubscriptionPlan, Bucket> rateLimitBuckets() {
        Map<SubscriptionPlan, Bucket> buckets = new ConcurrentHashMap<>();

        // FREE: 5/min + 20/hour
        buckets.put(SubscriptionPlan.FREE, Bucket.builder()
                .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                .addLimit(Bandwidth.classic(20, Refill.intervally(20, Duration.ofHours(1))))
                .build());

        // BASIC: 20/min (burst 50)
        buckets.put(SubscriptionPlan.BASIC, Bucket.builder()
                .addLimit(Bandwidth.classic(50, Refill.intervally(20, Duration.ofMinutes(1))))
                .build());

        // PRO: 60/min (burst 200)
        buckets.put(SubscriptionPlan.PRO, Bucket.builder()
                .addLimit(Bandwidth.classic(200, Refill.intervally(60, Duration.ofMinutes(1))))
                .build());

        return buckets;
    }
}
```

**Checklist**:
- [ ] Tạo file mới
- [ ] Implement rate limit buckets theo plan
- [ ] Test với các plan khác nhau

---

### Task 6.3: Create RateLimitInterceptor
**File**: `src/main/java/com/ftnam/image_ai_backend/interceptor/RateLimitInterceptor.java` (NEW)

**Action**:
```java
package com.ftnam.image_ai_backend.interceptor;

import com.ftnam.image_ai_backend.configuration.RateLimitConfig;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.UserRepository;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {
    private final RateLimitConfig rateLimitConfig;
    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Chỉ áp dụng cho /analyze endpoint
        if (!request.getRequestURI().contains("/analyze")) {
            return true;
        }

        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findById(userId)
                    .orElse(null);

            if (user == null) {
                // Anonymous user → FREE plan limit
                Bucket bucket = rateLimitConfig.rateLimitBuckets().get(SubscriptionPlan.FREE);
                if (!bucket.tryConsume(1)) {
                    throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED);
                }
                return true;
            }

            // Get bucket theo plan của user
            Bucket bucket = rateLimitConfig.rateLimitBuckets().get(user.getSubscription());
            if (bucket == null) {
                bucket = rateLimitConfig.rateLimitBuckets().get(SubscriptionPlan.FREE);
            }

            if (!bucket.tryConsume(1)) {
                throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED);
            }

            return true;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            // Log error nhưng không block request
            return true;
        }
    }
}
```

**Checklist**:
- [ ] Tạo file mới
- [ ] Implement interceptor
- [ ] Register interceptor trong `WebMvcConfigurer`
- [ ] Tạo `ErrorCode.RATE_LIMIT_EXCEEDED`

---

### Task 6.4: Register Interceptor
**File**: `src/main/java/com/ftnam/image_ai_backend/configuration/WebConfig.java` (NEW hoặc update existing)

**Action**:
```java
package com.ftnam.image_ai_backend.configuration;

import com.ftnam.image_ai_backend.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/analyze/**");
    }
}
```

**Checklist**:
- [ ] Tạo/update WebConfig
- [ ] Register interceptor
- [ ] Test rate limiting với các plan

---

## 📋 PHASE 7: DATA MIGRATION & SEEDING (Priority: HIGH)

### Task 7.1: Create Migration Script
**File**: `src/main/resources/db/migration/V2__update_plan_structure.sql` (hoặc dùng Flyway/Liquibase)

**Action**:
```sql
-- 1. Update PlanInfo table
ALTER TABLE subscription_plan_info 
  CHANGE COLUMN weekly_credit monthly_credit INT NOT NULL,
  ADD COLUMN max_credit INT NOT NULL DEFAULT 50 AFTER monthly_credit;

-- 2. Update User table
ALTER TABLE user 
  ADD COLUMN subscription_locked_until DATETIME NULL AFTER subscription_expired_at,
  ADD COLUMN payment_grace_until DATETIME NULL AFTER subscription_locked_until,
  ADD COLUMN version BIGINT NOT NULL DEFAULT 0 AFTER payment_grace_until;

-- 3. Update enum: PREMIUM → BASIC (nếu MySQL)
-- Note: MySQL không hỗ trợ rename enum trực tiếp, cần ALTER TABLE
ALTER TABLE subscription_plan_info 
  MODIFY COLUMN subscription ENUM('FREE', 'BASIC', 'PRO') NOT NULL;

ALTER TABLE user 
  MODIFY COLUMN subscription ENUM('FREE', 'BASIC', 'PRO') NOT NULL;

-- 4. Update existing PlanInfo data
UPDATE subscription_plan_info 
SET subscription = 'BASIC' 
WHERE subscription = 'PREMIUM';

-- 5. Seed PlanInfo với data mới
DELETE FROM subscription_plan_info;

INSERT INTO subscription_plan_info (subscription, monthly_credit, max_credit, price) VALUES
('FREE', 50, 50, 0),
('BASIC', 600, 800, 49000),
('PRO', 2000, 2500, 99000);
```

**Checklist**:
- [ ] Tạo migration script
- [ ] Test trên dev database
- [ ] Backup production trước khi chạy
- [ ] Verify data sau migration

---

### Task 7.2: Update DataInitializer
**File**: `src/main/java/com/ftnam/image_ai_backend/configuration/DataInitializer.java`

**Action**:
```java
private void seedAdminUser(){
    // ... existing role creation ...

    // Update PlanInfo seeding
    PlanInfo freePlan = PlanInfo.builder()
            .subscription(SubscriptionPlan.FREE)
            .price(0)
            .monthlyCredit(50)  // ĐỔI: weeklyCredit → monthlyCredit
            .maxCredit(50)      // THÊM
            .build();

    PlanInfo basicPlan = PlanInfo.builder()
            .subscription(SubscriptionPlan.BASIC)
            .price(49000)
            .monthlyCredit(600)
            .maxCredit(800)
            .build();

    PlanInfo proPlan = PlanInfo.builder()
            .subscription(SubscriptionPlan.PRO)
            .price(99000)
            .monthlyCredit(2000)
            .maxCredit(2500)
            .build();

    // Save plans nếu chưa có
    if (planInfoRepository.count() == 0) {
        planInfoRepository.saveAll(List.of(freePlan, basicPlan, proPlan));
    }

    // ... rest of admin user creation ...
}
```

**Checklist**:
- [ ] Update PlanInfo seeding với monthlyCredit + maxCredit
- [ ] Đổi PREMIUM → BASIC
- [ ] Test seeding trên fresh database

---

## 📋 PHASE 8: ERROR CODES & DTOs (Priority: MEDIUM)

### Task 8.1: Add New Error Codes
**File**: `src/main/java/com/ftnam/image_ai_backend/exception/ErrorCode.java`

**Action**:
```java
public enum ErrorCode {
    // ... existing codes ...
    
    SUBSCRIPTION_LOCKED(4001, "Subscription is locked. Cannot downgrade/refund during lock period"),
    RATE_LIMIT_EXCEEDED(4002, "Rate limit exceeded. Please try again later"),
    INVALID_CREDIT_COST(4003, "Invalid credit cost calculation");
}
```

**Checklist**:
- [ ] Thêm error codes mới
- [ ] Update exception handler nếu cần

---

### Task 8.2: Update DTOs
**Files**: 
- `PlanInfoRequest.java`
- `PlanInfoResponse.java`

**Action**:
```java
// PlanInfoRequest.java
public class PlanInfoRequest {
    private SubscriptionPlan subscription;
    private int monthlyCredit;  // ĐỔI: weeklyCredit → monthlyCredit
    private int maxCredit;      // THÊM
    private int price;
}

// PlanInfoResponse.java
public class PlanInfoResponse {
    private String id;
    private SubscriptionPlan subscription;
    
    @JsonProperty("monthly_credit")  // ĐỔI
    private int monthlyCredit;
    
    @JsonProperty("max_credit")      // THÊM
    private int maxCredit;
    
    private int price;
}
```

**Checklist**:
- [ ] Update Request DTO
- [ ] Update Response DTO
- [ ] Update Mapper nếu cần

---

## 📋 PHASE 9: TESTING (Priority: HIGH)

### Task 9.1: Unit Tests - CreditCostCalculator
**File**: `src/test/java/com/ftnam/image_ai_backend/service/CreditCostCalculatorTest.java` (NEW)

**Action**:
```java
@SpringBootTest
class CreditCostCalculatorTest {
    @Autowired
    CreditCostCalculator calculator;

    @Test
    void testDogOnly() {
        AnalyzeResponse response = AnalyzeResponse.builder()
                .animal("DOG")
                .breed(null)
                .build();
        assertEquals(1, calculator.calculateCost(response));
    }

    @Test
    void testDogWithBreed() {
        AnalyzeResponse response = AnalyzeResponse.builder()
                .animal("DOG")
                .breed("Golden Retriever")
                .build();
        assertEquals(2, calculator.calculateCost(response));
    }

    // ... more test cases ...
}
```

**Checklist**:
- [ ] Test DOG only → 1 credit
- [ ] Test DOG + breed → 2 credits
- [ ] Test CAT only → 1 credit
- [ ] Test CAT + breed → 2 credits
- [ ] Test OTHER → 1 credit

---

### Task 9.2: Unit Tests - CreditService
**File**: `src/test/java/com/ftnam/image_ai_backend/service/CreditServiceTest.java` (NEW)

**Action**:
```java
@SpringBootTest
@Transactional
class CreditServiceTest {
    @Autowired
    CreditService creditService;
    
    @Autowired
    UserRepository userRepository;
    
    @Autowired
    PlanInfoRepository planInfoRepository;

    @Test
    void testMonthlyReset() {
        // Setup: user với creditResetAt = 31 ngày trước
        User user = createUserWithOldResetDate(31);
        
        // Execute
        creditService.ensureCreditValid(user);
        
        // Verify: credit được reset với hard cap
        User updated = userRepository.findById(user.getId()).orElseThrow();
        assertEquals(planInfo.getMaxCredit(), updated.getCredit());
    }

    // ... more test cases ...
}
```

**Checklist**:
- [ ] Test monthly reset sau 30 ngày
- [ ] Test hard cap không vượt maxCredit
- [ ] Test không reset nếu chưa đủ 30 ngày

---

### Task 9.3: Integration Tests - Payment Flow
**File**: `src/test/java/com/ftnam/image_ai_backend/integration/PaymentIntegrationTest.java` (NEW)

**Checklist**:
- [ ] Test upgrade: cộng monthlyCredit với hard cap
- [ ] Test lock period được set đúng
- [ ] Test downgrade prevention khi locked
- [ ] Test payment fail grace period

---

## 📋 PHASE 10: FRONTEND UPDATES (Priority: MEDIUM)

### Task 10.1: Update Plan Names
**Files**: 
- `src/pages/Plan.js`
- `src/pages/Plans.js`

**Action**:
```javascript
// Plan.js - Update hardcoded plans
const [plans, setPlans] = useState([
  {
    id: "free-plan",
    subscription: "FREE",
    price: 0,
    monthlyCredit: 50,  // ĐỔI: weeklyCredit → monthlyCredit
    maxCredit: 50,      // THÊM
    features: [...]
  },
  {
    id: "basic-plan",   // ĐỔI: premium-plan → basic-plan
    subscription: "BASIC",  // ĐỔI: PREMIUM → BASIC
    price: 49000,       // Update giá
    monthlyCredit: 600,
    maxCredit: 800,
    features: [...]
  },
  // ...
]);
```

**Checklist**:
- [ ] Đổi PREMIUM → BASIC
- [ ] Update price: 200k → 49k
- [ ] Update credit: weeklyCredit → monthlyCredit
- [ ] Thêm maxCredit vào UI

---

### Task 10.2: Show Credit Cost in UI
**File**: `src/pages/Home.js`

**Action**:
```javascript
// Show credit cost khi analyze
const handleAnalyze = async () => {
  // ... existing code ...
  
  // Show cost info
  setCreditCostInfo({
    base: 1,
    breed: 1,  // +1 nếu có breed
    total: "1-2 credits"  // Dynamic based on result
  });
};
```

**Checklist**:
- [ ] Show credit cost trước khi analyze
- [ ] Update cost sau khi có kết quả
- [ ] Show warning khi credit thấp (20%/10%/5%)

---

### Task 10.3: Credit History Page (Optional)
**File**: `src/pages/CreditHistory.js` (NEW)

**Checklist**:
- [ ] Tạo page mới
- [ ] Fetch credit history từ API
- [ ] Show: time, action, credit used, image

---

## ✅ FINAL CHECKLIST

### Before Deploy
- [ ] Tất cả unit tests pass
- [ ] Integration tests pass
- [ ] Database migration tested trên staging
- [ ] Frontend updated và tested
- [ ] API documentation updated
- [ ] Error messages user-friendly

### Deployment Steps
1. [ ] Backup production database
2. [ ] Run migration script
3. [ ] Deploy backend
4. [ ] Deploy frontend
5. [ ] Verify payment flow
6. [ ] Monitor logs for errors
7. [ ] Verify credit reset cron job

---

## 📝 NOTES

- **Optimistic Locking**: Cần test kỹ với concurrent requests
- **Rate Limiting**: Có thể dùng Redis thay vì in-memory nếu scale
- **Credit History**: Nên track mọi credit transaction để audit
- **Monitoring**: Thêm metrics cho credit usage, payment success rate

---

**Last Updated**: 2025-01-XX
**Status**: Ready for Implementation

