package com.apisecurity.platform.service;

import com.apisecurity.platform.domain.user.UserEntity;
import com.apisecurity.platform.dto.AuthDto.*;
import com.apisecurity.platform.exception.UnauthorizedException;
import com.apisecurity.platform.repository.UserRepository;
import com.apisecurity.platform.security.ApiKeyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User already registered with email: " + request.getEmail());
        }

        String hashedPassword = ApiKeyUtils.hashApiKey(request.getPassword());
        String role = (request.getRole() != null && !request.getRole().isBlank()) ? request.getRole() : "Security Analyst";

        UserEntity user = UserEntity.builder()
                .email(request.getEmail().trim().toLowerCase())
                .passwordHash(hashedPassword)
                .role(role)
                .build();

        userRepository.save(user);

        String token = "soc_jwt_" + UUID.randomUUID().toString().replace("-", "");
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .message("Operator registered successfully")
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        String hashedInputPass = ApiKeyUtils.hashApiKey(request.getPassword());
        if (!user.getPasswordHash().equals(hashedInputPass)) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = "soc_jwt_" + UUID.randomUUID().toString().replace("-", "");
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .message("Authentication successful")
                .build();
    }
}
