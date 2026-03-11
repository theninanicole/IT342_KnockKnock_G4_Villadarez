package com.knockknock.backend.service;

import com.knockknock.backend.dto.*;
import com.knockknock.backend.entity.User;
import com.knockknock.backend.repository.UserRepository;
import com.knockknock.backend.repository.CondoRepository;
import com.knockknock.backend.security.JwtUtils;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CondoRepository condoRepository;

    public AuthServiceImpl(UserRepository userRepository, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager, CondoRepository condoRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.condoRepository = condoRepository;
    }

    @Override
    public Object registerVisitor(RegisterVisitorRequest request) {
        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        // Validate password length
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("VISITOR");
        user.setAuthProvider("email");
        if (request.getContactNumber() != null) {
            try {
                user.setContactNumber(Long.parseLong(request.getContactNumber()));
            } catch (NumberFormatException e) {
                user.setContactNumber(null);
            }
        }
        userRepository.save(user);
        String token = jwtUtils.generateJwtToken(user.getEmail());
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getContactNumber(),
            user.getAuthProvider(),
            user.getCondo()
        );
        return java.util.Map.of(
            "user", userResponse,
            "token", token
        );
    }

    @Override
    public Object registerCondoAdmin(RegisterCondoAdminRequest request) {

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Validate password length
        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // ---------- Create Condo ----------
        com.knockknock.backend.entity.Condo condo = new com.knockknock.backend.entity.Condo();

        condo.setName(request.getCondoName());
        condo.setAddress(request.getCondoAddress());
        condo.setStatus("ACTIVE");

        // ---------- Generate Unique Condo Code ----------
        String baseCode = "CON";

        if (request.getCondoName() != null && request.getCondoName().length() >= 3) {
            baseCode = request.getCondoName().substring(0, 3).toUpperCase();
        }

        String code = baseCode;
        int counter = 1;

        while (condoRepository.existsByCode(code)) {
            code = baseCode + counter;
            counter++;
        }

        condo.setCode(code);

        // Save condo
        condoRepository.save(condo);

        // ---------- Create Admin User ----------
        User user = new User();

        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("CONDOMINIUM_ADMIN");
        user.setAuthProvider("email");
        if (request.getContactNumber() != null) {
            try {
                user.setContactNumber(Long.parseLong(request.getContactNumber()));
            } catch (NumberFormatException e) {
                user.setContactNumber(null);
            }
        }
        // Link admin to condo
        user.setCondo(condo);
        userRepository.save(user);

        // ---------- Generate JWT ----------
        String token = jwtUtils.generateJwtToken(user.getEmail());

        // ---------- Prepare Response ----------
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getContactNumber(),
                user.getAuthProvider(),
                user.getCondo()
        );

        return java.util.Map.of(
                "user", userResponse,
                "token", token,
                "message", "Condominium registered successfully. Your condo code is: " + code
        );
    }

    @Override
    public Object login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));
        if ("google".equalsIgnoreCase(user.getAuthProvider())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED,
                "Please login with Google"
            );
        }
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication.getName());
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("email", user.getEmail());
        result.put("fullName", user.getFullName());
        result.put("role", user.getRole());
        result.put("token", token);
        return result;
    }
}