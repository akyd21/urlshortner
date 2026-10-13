package com.urlshortener.service;

import com.urlshortener.dto.AuthRequest;
import com.urlshortener.dto.AuthResponse;
import com.urlshortener.entity.User;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(AuthRequest request) {
        String email = normalizedEmail(request.getEmail());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.getPassword())));
        return new AuthResponse(jwtService.createToken(user.getEmail()), user.getEmail());
    }

    public AuthResponse login(AuthRequest request) {
        String email = normalizedEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }
        return new AuthResponse(jwtService.createToken(user.getEmail()), user.getEmail());
    }

    private String normalizedEmail(String email) {
        return email.trim().toLowerCase();
    }
}
