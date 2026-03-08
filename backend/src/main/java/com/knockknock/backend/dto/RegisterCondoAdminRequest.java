package com.knockknock.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterCondoAdminRequest {
    private String contactNumber;
    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    @NotBlank
    private String fullName;

    @Email
    private String email;

    @NotBlank
    private String password;

    private String confirmPassword;
    private String condoName;
    private String condoAddress;
    private String condoContact;

    public RegisterCondoAdminRequest() {}
    public String getConfirmPassword() {
        return confirmPassword;
    }
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}