package com.ftnam.image_ai_backend.service.impl;

import com.ftnam.image_ai_backend.dto.request.AuthenticationRequest;
import com.ftnam.image_ai_backend.dto.request.LogoutRequest;
import com.ftnam.image_ai_backend.dto.request.RefreshRequest;
import com.ftnam.image_ai_backend.dto.response.AuthenticationResponse;
import com.ftnam.image_ai_backend.entity.InvalidatedToken;
import com.ftnam.image_ai_backend.entity.RefreshTokenRedis;
import com.ftnam.image_ai_backend.entity.User;
import com.ftnam.image_ai_backend.exception.AppException;
import com.ftnam.image_ai_backend.exception.ErrorCode;
import com.ftnam.image_ai_backend.repository.InvalidatedTokenRepository;
import com.ftnam.image_ai_backend.repository.RefreshTokenRedisRepository;
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
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    RefreshTokenRedisRepository refreshTokenRedisRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal
    @Value("${jwt.signer-key}")
    String signerKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    Long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    Long refreshableDuration;

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        Optional<RefreshTokenRedis> isHasRefreshToken = refreshTokenRedisRepository.findById(user.getId());
        if(isHasRefreshToken.isPresent()){
            refreshTokenRedisRepository.deleteById(user.getId());
        }

        var accessToken = generateToken(user, false);
        var refreshToken = generateToken(user,true);

        RefreshTokenRedis refreshTokenRedis = RefreshTokenRedis.builder()
                .id(user.getId())
                .token(refreshToken)
                .expirationTime(refreshableDuration)
                .build();

        refreshTokenRedisRepository.save(refreshTokenRedis);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getRefreshToken());
        String jit = signedToken.getJWTClaimsSet().getJWTID();
        Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime();
        String userId = signedToken.getJWTClaimsSet().getSubject();

        Optional<RefreshTokenRedis> storedToken = refreshTokenRedisRepository.findById(userId);
        if(storedToken.isEmpty() || !storedToken.get().getToken().equals(request.getRefreshToken())){
            log.error("Refresh token in redis is empty or invalid");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        refreshTokenRedisRepository.deleteById(userId);

        var user = userRepository.findById(userId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_EXISTED));

        String accessToken = generateToken(user, false);
        String refreshToken = generateToken(user, true);

        RefreshTokenRedis refreshTokenRedis = RefreshTokenRedis.builder()
                .id(userId)
                .token(refreshToken)
                .expirationTime(refreshableDuration)
                .build();

        refreshTokenRedisRepository.save(refreshTokenRedis);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        var signedToken = verifyToken(request.getAccessToken());

        String userId = signedToken.getJWTClaimsSet().getSubject();
        String jit = signedToken.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jit)
                .expiryTime(expiryTime)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
        refreshTokenRedisRepository.deleteById(userId);
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());

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

        long expirationTime = isRefreshToken ? refreshableDuration : validDuration;

        JWTClaimsSet.Builder claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("ftnam.com")
                .issueTime(new java.util.Date())
                .expirationTime(Date.from(Instant.now().plus(expirationTime, ChronoUnit.SECONDS)))
                .jwtID(UUID.randomUUID().toString());

        if(!isRefreshToken){
            claimsSet.claim("scope", buildScope(user));
        }

        JWTClaimsSet jwtClaimsSet = claimsSet.build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header,payload);

        try {
            jwsObject.sign(new MACSigner(signerKey));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Can not create token");
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())){
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions()
                            .forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }
        return stringJoiner.toString();
    }
}
