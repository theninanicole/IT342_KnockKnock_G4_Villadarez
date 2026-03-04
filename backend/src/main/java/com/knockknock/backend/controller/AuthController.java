package com.knockknock.backend.controller;

import com.knockknock.backend.dto.LoginRequest;
import com.knockknock.backend.dto.RegisterCondoAdminRequest;
import com.knockknock.backend.dto.RegisterVisitorRequest;
import com.knockknock.backend.service.AuthService;

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
            @RequestBody RegisterVisitorRequest request) {

        return ResponseEntity.ok(authService.registerVisitor(request));
    }

    @PostMapping("/register/condo-admin")
    public ResponseEntity<?> registerCondoAdmin(
            @RequestBody RegisterCondoAdminRequest request) {

        return ResponseEntity.ok(authService.registerCondoAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }
}