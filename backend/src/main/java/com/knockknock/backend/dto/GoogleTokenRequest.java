package com.knockknock.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleTokenRequest {

    @NotBlank
    private String idToken;

    public GoogleTokenRequest() {}

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
