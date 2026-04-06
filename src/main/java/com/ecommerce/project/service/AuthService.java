package com.ecommerce.project.service;

import com.ecommerce.project.dto.LoginResponse;
import com.ecommerce.project.dto.UserRequest;
import com.ecommerce.project.dto.UserResponse;
import com.ecommerce.project.entity.Role;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.exception.AuthenticationException;
import com.ecommerce.project.exception.ConflictException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserResponse register(UserRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.CUSTOMER)
                .build();

        User savedUser = userRepository.saveAndFlush(user);

        return toResponse(savedUser);
    }

    public LoginResponse login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid Credentials"
                ));

        if(!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new AuthenticationException("Invalid Credentials");
        }

        UserResponse userResponse = toResponse(user);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(userResponse, accessToken, refreshToken);
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if(jwtService.isTokenValid(refreshToken, user.getEmail())) {
            String newAccessToken = jwtService.generateToken(user);
            return new LoginResponse(toResponse(user), newAccessToken, refreshToken);
        }

        throw new AuthenticationException("Invalid Refresh Token");
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
