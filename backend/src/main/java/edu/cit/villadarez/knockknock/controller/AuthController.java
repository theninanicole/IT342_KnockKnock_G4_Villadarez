package edu.cit.villadarez.knockknock.controller;

import edu.cit.villadarez.knockknock.dto.LoginRequest;
import edu.cit.villadarez.knockknock.dto.GoogleTokenRequest;
import edu.cit.villadarez.knockknock.dto.RegisterCondoAdminRequest;
import edu.cit.villadarez.knockknock.dto.RegisterCondoAdminGoogleRequest;
import edu.cit.villadarez.knockknock.dto.RegisterVisitorRequest;
import edu.cit.villadarez.knockknock.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(originPatterns = "*", allowCredentials = "true")
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

    @PostMapping("/register/visitor/google")
    public ResponseEntity<?> registerVisitorWithGoogle(
            @Valid @RequestBody GoogleTokenRequest request) {
        Object body = authService.registerVisitorWithGoogle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/register/condo-admin")
    public ResponseEntity<?> registerCondoAdmin(
            @Valid @RequestBody RegisterCondoAdminRequest request) {
        Object body = authService.registerCondoAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/register/condo-admin/google")
    public ResponseEntity<?> registerCondoAdminWithGoogle(
            @Valid @RequestBody RegisterCondoAdminGoogleRequest request) {
        Object body = authService.registerCondoAdminWithGoogle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<?> loginWithGoogle(
            @Valid @RequestBody GoogleTokenRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Missing or invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        Object body = authService.getCurrentUser(token);
        return ResponseEntity.ok(body);
    }
}
