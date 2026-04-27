package com.testai.ai_api_tester.controller;

import com.testai.ai_api_tester.config.JwtUtil;
import com.testai.ai_api_tester.dto.ApiResponse;
import com.testai.ai_api_tester.entity.User;
import com.testai.ai_api_tester.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Handles user registration and authentication.
 * Returns a signed JWT on success — no session is created.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Register a new user account and return a JWT for immediate login.
     *
     * @param request email + password (validated)
     * @return JWT token and email on success; error if email is already taken
     */
    @PostMapping("/signup")
    public ApiResponse<Map<String, String>> signup(@Valid @RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.error("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        String token = jwtUtil.generateToken(request.getEmail());
        log.info("User registered: {}", request.getEmail());
        return ApiResponse.ok(Map.of("token", token, "email", request.getEmail()));
    }

    /**
     * Authenticate an existing user and return a JWT.
     *
     * @param request email + password
     * @return JWT token and email on success; generic error to avoid user enumeration
     */
    @PostMapping("/signin")
    public ApiResponse<Map<String, String>> signin(@Valid @RequestBody AuthRequest request) {
        return userRepository.findByEmail(request.getEmail())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getEmail());
                    log.info("User signed in: {}", u.getEmail());
                    return ApiResponse.ok(Map.of("token", token, "email", u.getEmail()));
                })
                .orElse(ApiResponse.error("Invalid email or password"));
    }

    @Data
    static class AuthRequest {
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        private String password;
    }
}
