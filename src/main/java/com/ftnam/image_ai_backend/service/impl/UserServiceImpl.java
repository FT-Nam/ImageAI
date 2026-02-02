package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.event.EmailEvent;
import com.ftnam.image_ai_backend.dto.request.UserCreationRequest;
import com.ftnam.image_ai_backend.dto.request.UserUpdateRequest;
import com.ftnam.image_ai_backend.dto.response.UserResponse;
import com.ftnam.image_ai_backend.entity.Role;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.mapper.UserMapper;
import com.ftnam.image_ai_backend.repository.RoleRepository;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.UserService;
import com.ftnam.image_ai_backend.util.NotificationSender;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    NotificationSender notificationSender;
    KafkaTemplate<String,Object> kafkaTemplate;

    @Value("${brevo.api-key}")
    @NonFinal
    String apiKey;

    int creditInitial = 200;

    @Transactional
    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setCredit(creditInitial);
        user.setSubscription(SubscriptionPlan.FREE);
        user.setCreditResetAt(LocalDateTime.now());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findById("USER")
                .orElseThrow(()-> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        user.setRoles(Set.of(role));

        EmailEvent emailEvent = EmailEvent.builder()
                .channel("EMAIL")
                .recipient(request.getEmail())
                .templateId(1)
                .params(Map.of("name", request.getName()))
                .build();


        kafkaTemplate.send("email-delivery", emailEvent);

        notificationSender.sendNotification(user.getId(), "Welcome aboard! Your account has been created successfully.");

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toUserResponse);
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_NOT_EXISTED));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateUser(String id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_NOT_EXISTED));
        userMapper.updateUser(user, request);

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            user.setPassword(encodedPassword);
        }

        notificationSender.sendNotification(user.getId(), "Your profile has been update successfully");

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
