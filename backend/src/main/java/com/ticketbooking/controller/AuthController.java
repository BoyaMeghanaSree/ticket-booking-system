package com.ticketbooking.controller;

import com.ticketbooking.dto.LoginRequest;
import com.ticketbooking.entity.Role;
import com.ticketbooking.entity.User;
import com.ticketbooking.repository.UserRepository;
import com.ticketbooking.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UserRepository userRepository,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        if (userRepository.existsByEmail(request.email)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already registered");
        }

        User user = new User(
                request.name,
                request.email,
                passwordEncoder.encode(request.password),
                request.role);

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(
                new LoginResponse(
                        token,
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()));
    }

    public static class RegisterRequest {

        public String name;
        public String email;
        public String password;
        public Role role;
    }

    public static class LoginResponse {

        private final String token;
        private final Long userId;
        private final String name;
        private final String email;
        private final Role role;

        public LoginResponse(
                String token,
                Long userId,
                String name,
                String email,
                Role role) {

            this.token = token;
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public String getToken() {
            return token;
        }

        public Long getUserId() {
            return userId;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public Role getRole() {
            return role;
        }
    }
}