package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.UserCreationRequest;
import com.ftnam.image_ai_backend.dto.request.UserUpdateRequest;
import com.ftnam.image_ai_backend.dto.response.UserResponse;
import com.ftnam.image_ai_backend.entity.Role;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.enums.SubscriptionPlan;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.mapper.UserMapper;
import com.ftnam.image_ai_backend.repository.HistoryRepository;
import com.ftnam.image_ai_backend.repository.RoleRepository;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;

    int creditInitial = 200;

    @Override
    public UserResponse createUser(UserCreationRequest request) {
        User user = userMapper.toUser(request);
        user.setCredit(200);
        user.setSubscription(SubscriptionPlan.FREE);
        user.setCreditResetAt(LocalDateTime.now());

        Role role = roleRepository.findById("USER")
                .orElseThrow(()-> new AppException(ErrorCode.ROLE_NOT_EXISTED));

        user.setRoles(Set.of(role));

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
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
