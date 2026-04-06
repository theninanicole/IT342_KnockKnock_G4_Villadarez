package com.knockknock.backend.dto;

import jakarta.validation.constraints.NotBlank;

public class RegisterCondoAdminGoogleRequest extends GoogleTokenRequest {

    @NotBlank
    private String condoName;

    @NotBlank
    private String condoAddress;

    private String condoContact;

    public RegisterCondoAdminGoogleRequest() {}

    public String getCondoName() {
        return condoName;
    }

    public void setCondoName(String condoName) {
        this.condoName = condoName;
    }

    public String getCondoAddress() {
        return condoAddress;
    }

    public void setCondoAddress(String condoAddress) {
        this.condoAddress = condoAddress;
    }

    public String getCondoContact() {
        return condoContact;
    }

    public void setCondoContact(String condoContact) {
        this.condoContact = condoContact;
    }
}
