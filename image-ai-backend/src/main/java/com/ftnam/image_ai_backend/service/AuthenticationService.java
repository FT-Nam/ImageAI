package com.ftnam.image_ai_backend.service;

import com.ftnam.image_ai_backend.dto.request.AuthenticationRequest;
import com.ftnam.image_ai_backend.dto.request.LogoutRequest;
import com.ftnam.image_ai_backend.dto.request.RefreshRequest;
import com.ftnam.image_ai_backend.dto.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse refreshToken(RefreshRequest request);

    void logout(LogoutRequest request);
}
