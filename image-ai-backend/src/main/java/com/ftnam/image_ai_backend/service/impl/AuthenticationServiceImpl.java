package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.AuthenticationRequest;
import com.ftnam.image_ai_backend.dto.request.LogoutRequest;
import com.ftnam.image_ai_backend.dto.request.RefreshRequest;
import com.ftnam.image_ai_backend.dto.response.AuthenticationResponse;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.UserRepository;
import com.ftnam.image_ai_backend.service.AuthenticationService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;

    @Value("${jwt.signer-key}")
    String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    Long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    Long REFRESHABLE_DURATION;

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        var accessToken = generateToken(user, false);
        var refreshToken = generateToken(user,true);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) {
        return null;
    }

    @Override
    public void logout(LogoutRequest request) {

    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if(!(verified && expiryTime.after(new Date()))){
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }


        return signedJWT;
    }

    private String generateToken(User user, boolean isRefreshToken){
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        long expirationTime = isRefreshToken ? REFRESHABLE_DURATION : VALID_DURATION;

        JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("ftnam.com")
                .issueTime(new java.util.Date())
                .expirationTime(Date.from(Instant.now().plus(expirationTime, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString());

        JWTClaimsSet jwtClaimsSet = claimsSet.build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Can not create token");
            throw new RuntimeException(e);
        }

    }
}
