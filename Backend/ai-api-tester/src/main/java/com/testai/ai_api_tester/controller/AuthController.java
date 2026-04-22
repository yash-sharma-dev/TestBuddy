package com.testai.ai_api_tester.controller;


import com.testai.ai_api_tester.entity.User;
import com.testai.ai_api_tester.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestBody User user) {
        userRepository.save(user);

        return Map.of(
                "success", true,
                "message", "User registered"
        );
    }

    @PostMapping("/signin")
    public Map<String, Object> signin(@RequestBody User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());

        if (existing.isPresent() &&
                existing.get().getPassword().equals(user.getPassword())) {

            return Map.of(
                    "success", true,
                    "token", "dummy-token"
            );
        }

        return Map.of(
                "success", false,
                "message", "Invalid credentials"
        );
    }
}
