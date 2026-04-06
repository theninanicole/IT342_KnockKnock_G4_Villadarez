package com.knockknock.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "condos")
public class Condo {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "condo_id", columnDefinition = "UUID")
    private UUID condoId;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "address")
    private String address;

    @Column(name = "status")
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Default constructor
    public Condo() {
    }

    // All-args constructor
    public Condo(UUID condoId, String name, String code, String address, String contactNumber, String status, LocalDateTime createdAt) {
        this.condoId = condoId;
        this.name = name;
        this.code = code;
        this.address = address;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public UUID getCondoId() {
        return condoId;
    }

    public void setCondoId(UUID condoId) {
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}