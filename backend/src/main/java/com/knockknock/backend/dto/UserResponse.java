package com.knockknock.backend.dto;

import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private Long contactNumber;
    private String authProvider;
    private Object condo;
    private String token;

    public UserResponse(UUID id, String fullName, String email, String role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
    }

    public UserResponse(UUID id, String fullName, String email, String role, Long contactNumber, String authProvider, Object condo) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.contactNumber = contactNumber;
        this.authProvider = authProvider;
        this.condo = condo;
    }

    public UserResponse(UUID id, String fullName, String email, String role, Long contactNumber, String authProvider, Object condo, String token) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.contactNumber = contactNumber;
        this.authProvider = authProvider;
        this.condo = condo;
        this.token = token;
    }

    public UUID getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getContactNumber() { return contactNumber; }
    public String getAuthProvider() { return authProvider; }
    public Object getCondo() { return condo; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}