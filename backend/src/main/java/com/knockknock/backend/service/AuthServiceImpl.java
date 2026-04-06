package com.knockknock.backend.service;

import com.knockknock.backend.dto.*;
import com.knockknock.backend.entity.User;
import com.knockknock.backend.exception.OAuthProviderConflictException;
import com.knockknock.backend.repository.UserRepository;
import com.knockknock.backend.repository.CondoRepository;
import com.knockknock.backend.security.JwtUtils;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final CondoRepository condoRepository;

    @Value("${google.client-id}")
    private String googleClientId;

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
            user.getCondo(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
        return java.util.Map.of(
            "user", userResponse,
            "token", token
        );
    }

    @Override
    public Object registerVisitorWithGoogle(GoogleTokenRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String fullName = resolveGoogleName(payload);

        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingByGoogleId.isPresent()) {
            User existingUser = existingByGoogleId.get();
            existingUser.setFullName(fullName);
            existingUser.setEmail(email);
            ensureInternalPasswordForGoogleUser(existingUser);
            userRepository.save(existingUser);
            return buildAuthResponse(existingUser);
        }

        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User existingUser = existingByEmail.get();
            if (!"google".equalsIgnoreCase(existingUser.getAuthProvider())) {
                throw new OAuthProviderConflictException("Account exists with email/password. Please login using email and password.");
            }
            existingUser.setGoogleId(googleId);
            existingUser.setFullName(fullName);
            ensureInternalPasswordForGoogleUser(existingUser);
            userRepository.save(existingUser);
            return buildAuthResponse(existingUser);
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole("VISITOR");
        user.setAuthProvider("google");
        user.setGoogleId(googleId);
        ensureInternalPasswordForGoogleUser(user);
        userRepository.save(user);

        return buildAuthResponse(user);
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
            user.getCondo(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );

        return java.util.Map.of(
                "user", userResponse,
                "token", token,
                "message", "Condominium registered successfully. Your condo code is: " + code
        );
    }

    @Override
    public Object registerCondoAdminWithGoogle(RegisterCondoAdminGoogleRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String fullName = resolveGoogleName(payload);

        if (userRepository.existsByEmail(email) || userRepository.existsByGoogleId(googleId)) {
            throw new IllegalArgumentException("Email already registered");
        }

        com.knockknock.backend.entity.Condo condo = new com.knockknock.backend.entity.Condo();
        condo.setName(request.getCondoName());
        condo.setAddress(request.getCondoAddress());
        condo.setStatus("ACTIVE");

        String baseCode = "CON";
        if (request.getCondoName() != null && request.getCondoName().length() >= 3) {
            baseCode = request.getCondoName().substring(0, 3).toUpperCase();
        }
        String code = generateUniqueCondoCode(baseCode);
        condo.setCode(code);
        condoRepository.save(condo);

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole("CONDOMINIUM_ADMIN");
        user.setAuthProvider("google");
        user.setGoogleId(googleId);
        user.setCondo(condo);
        ensureInternalPasswordForGoogleUser(user);
        userRepository.save(user);

        String token = jwtUtils.generateJwtToken(user.getEmail());
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getContactNumber(),
                user.getAuthProvider(),
            user.getCondo(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );

        return Map.of(
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
        UserResponse userResponse = new UserResponse(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole(),
            user.getContactNumber(),
            user.getAuthProvider(),
            user.getCondo(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
        Map<String, Object> result = new HashMap<>();
        result.put("user", userResponse);
        result.put("token", token);
        return result;
    }

    @Override
    public Object loginWithGoogle(GoogleTokenRequest request) {
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());
        String email = payload.getEmail();
        String googleId = payload.getSubject();
        String fullName = resolveGoogleName(payload);

        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleId);
        if (existingByGoogleId.isPresent()) {
            User user = existingByGoogleId.get();
            user.setEmail(email);
            user.setFullName(fullName);
            ensureInternalPasswordForGoogleUser(user);
            userRepository.save(user);
            return buildAuthResponse(user);
        }

        Optional<User> existingByEmail = userRepository.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            if (!"google".equalsIgnoreCase(user.getAuthProvider())) {
                throw new OAuthProviderConflictException("Account exists with email/password. Please login using email and password.");
            }
            user.setGoogleId(googleId);
            user.setFullName(fullName);
            ensureInternalPasswordForGoogleUser(user);
            userRepository.save(user);
            return buildAuthResponse(user);
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRole("VISITOR");
        user.setAuthProvider("google");
        user.setGoogleId(googleId);
        ensureInternalPasswordForGoogleUser(user);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    private String generateUniqueCondoCode(String baseCode) {
        String code = baseCode;
        int counter = 1;
        while (condoRepository.existsByCode(code)) {
            code = baseCode + counter;
            counter++;
        }
        return code;
    }

    private Map<String, Object> buildAuthResponse(User user) {
        String token = jwtUtils.generateJwtToken(user.getEmail());
        UserResponse userResponse = new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getContactNumber(),
                user.getAuthProvider(),
            user.getCondo(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
        return Map.of(
                "user", userResponse,
                "token", token
        );
    }

    @Override
    public Object getCurrentUser(String token) {
        try {
            String email = jwtUtils.getUserEmailFromJwtToken(token);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getContactNumber(),
                    user.getAuthProvider(),
                    user.getCondo(),
                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
            );

            return Map.of("user", userResponse);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
        }
    }

    private String resolveGoogleName(GoogleIdToken.Payload payload) {
        Object name = payload.get("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        return payload.getEmail();
    }

    private void ensureInternalPasswordForGoogleUser(User user) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            String generatedRawPassword = "GOOGLE_" + UUID.randomUUID();
            user.setPassword(passwordEncoder.encode(generatedRawPassword));
        }
    }
}