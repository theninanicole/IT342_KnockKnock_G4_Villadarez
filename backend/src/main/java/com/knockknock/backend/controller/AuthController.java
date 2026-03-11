package com.knockknock.backend.controller;

import com.knockknock.backend.dto.LoginRequest;
import com.knockknock.backend.dto.RegisterCondoAdminRequest;
import com.knockknock.backend.dto.RegisterVisitorRequest;
import com.knockknock.backend.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/visitor")
    public ResponseEntity<?> registerVisitor(
            @Valid @RequestBody RegisterVisitorRequest request) {
        Object body = authService.registerVisitor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/register/condo-admin")
    public ResponseEntity<?> registerCondoAdmin(
            @Valid @RequestBody RegisterCondoAdminRequest request) {
        Object body = authService.registerCondoAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}