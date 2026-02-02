package com.ftnam.image_ai_backend.util;

import com.ftnam.image_ai_backend.dto.request.NotificationCreationRequest;
import com.ftnam.image_ai_backend.service.NotificationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationSender {
    NotificationService notificationService;

    public void sendNotification(String userId, String content) {
        notificationService.createNotification(
                NotificationCreationRequest.builder()
                        .userId(userId)
                        .content(content)
                        .build()
        );
    }
}

