package com.skillbridge.service;

import com.skillbridge.dto.AuthResponse;
import com.skillbridge.dto.LoginRequest;
import com.skillbridge.dto.RegisterRequest;
import com.skillbridge.exception.ResourceNotFoundException;
import com.skillbridge.model.User;
import com.skillbridge.model.enums.Role;
import com.skillbridge.repository.UserRepository;
import com.skillbridge.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    // Manual constructor injection — avoids circular dependency
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider        = tokenProvider;
    }

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(
                request.getEmail().trim().toLowerCase())) {
            throw new IllegalArgumentException(
                    "Email already in use: " + request.getEmail());
        }

        Role role = (request.getRole() != null)
                ? request.getRole() : Role.ROLE_LEARNER;

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        log.info("Registered: {} role: {}", saved.getEmail(), saved.getRole());
        return saved;
    }

    public AuthResponse login(LoginRequest request,
                              HttpServletResponse response) {
        String email = request.getEmail().trim().toLowerCase();
        String password = request.getPassword();

        log.debug("Login attempt for: {}", email);

        // Authenticate against our DaoAuthenticationProvider
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        log.debug("Authentication success: {}", auth.isAuthenticated());

        String token = tokenProvider.generateToken(auth);

        // Set cookie for Thymeleaf
        Cookie cookie = new Cookie("JWT_TOKEN", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400);
        response.addCookie(cookie);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        log.info("Login success: {} role: {}", user.getEmail(), user.getRole());

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.getName(),
                user.getId()
        );
    }

    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("JWT_TOKEN", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public User getCurrentUser(String email) {
        return userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found: " + email));
    }
}