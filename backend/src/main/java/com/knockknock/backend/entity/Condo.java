package com.knockknock.backend.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "condos")
public class Condo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condo_id")
    private Long condoId;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "address")
    private String address;

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Default constructor
    public Condo() {
    }

    // All-args constructor
    public Condo(Long condoId, String name, String code, String address, String contactNumber, LocalDateTime createdAt) {
        this.condoId = condoId;
        this.name = name;
        this.code = code;
        this.address = address;
        this.contactNumber = contactNumber;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Long getCondoId() {
        return condoId;
    }

    public void setCondoId(Long condoId) {
        this.condoId = condoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}