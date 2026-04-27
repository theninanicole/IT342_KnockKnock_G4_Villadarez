package edu.cit.villadarez.knockknock.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "visits")
public class Visit {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "visit_id", columnDefinition = "UUID")
    private UUID visitId;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @JsonManagedReference
    private User visitor;

    @ManyToOne
    @JoinColumn(name = "condo_id", referencedColumnName = "condo_id", nullable = false)
    @JsonManagedReference
    private Condo condo;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "unit_number")
    private String unitNumber;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "visit_date")
    private LocalDate visitDate;

    @Column(name = "status")
    private String status;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "qr_image_url")
    private String qrImageUrl;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonIgnore
    private List<VisitFile> visitFiles;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public Visit() {}

    public Visit(User visitor, Condo condo, String referenceNumber, String unitNumber,
                 String purpose, LocalDate visitDate, String status) {
        this.visitor = visitor;
        this.condo = condo;
        this.referenceNumber = referenceNumber;
        this.unitNumber = unitNumber;
        this.purpose = purpose;
        this.visitDate = visitDate;
        this.status = status;
    }

    private Visit(Builder builder) {
        this.visitId = builder.visitId;
        this.visitor = builder.visitor;
        this.condo = builder.condo;
        this.referenceNumber = builder.referenceNumber;
        this.unitNumber = builder.unitNumber;
        this.purpose = builder.purpose;
        this.visitDate = builder.visitDate;
        this.status = builder.status;
        this.checkInTime = builder.checkInTime;
        this.checkOutTime = builder.checkOutTime;
        this.qrImageUrl = builder.qrImageUrl;
        this.visitFiles = builder.visitFiles;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Getters and Setters
    public UUID getVisitId() {
        return visitId;
    }

    public void setVisitId(UUID visitId) {
        this.visitId = visitId;
    }

    public User getVisitor() {
        return visitor;
    }

    public void setVisitor(User visitor) {
        this.visitor = visitor;
    }

    public Condo getCondo() {
        return condo;
    }

    public void setCondo(Condo condo) {
        this.condo = condo;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getUnitNumber() {
        return unitNumber;
    }

    public void setUnitNumber(String unitNumber) {
        this.unitNumber = unitNumber;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public String getQrImageUrl() {
        return qrImageUrl;
    }

    public void setQrImageUrl(String qrImageUrl) {
        this.qrImageUrl = qrImageUrl;
    }

    public List<VisitFile> getVisitFiles() {
        return visitFiles;
    }

    public void setVisitFiles(List<VisitFile> visitFiles) {
        this.visitFiles = visitFiles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID visitId;
        private User visitor;
        private Condo condo;
        private String referenceNumber;
        private String unitNumber;
        private String purpose;
        private LocalDate visitDate;
        private String status;
        private LocalDateTime checkInTime;
        private LocalDateTime checkOutTime;
        private String qrImageUrl;
        private List<VisitFile> visitFiles;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder visitId(UUID visitId) {
            this.visitId = visitId;
            return this;
        }

        public Builder visitor(User visitor) {
            this.visitor = visitor;
            return this;
        }

        public Builder condo(Condo condo) {
            this.condo = condo;
            return this;
        }

        public Builder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder unitNumber(String unitNumber) {
            this.unitNumber = unitNumber;
            return this;
        }

        public Builder purpose(String purpose) {
            this.purpose = purpose;
            return this;
        }

        public Builder visitDate(LocalDate visitDate) {
            this.visitDate = visitDate;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder checkInTime(LocalDateTime checkInTime) {
            this.checkInTime = checkInTime;
            return this;
        }

        public Builder checkOutTime(LocalDateTime checkOutTime) {
            this.checkOutTime = checkOutTime;
            return this;
        }

        public Builder qrImageUrl(String qrImageUrl) {
            this.qrImageUrl = qrImageUrl;
            return this;
        }

        public Builder visitFiles(List<VisitFile> visitFiles) {
            this.visitFiles = visitFiles;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Visit build() {
            if (visitor == null) {
                throw new IllegalStateException("visitor is required");
            }
            if (condo == null) {
                throw new IllegalStateException("condo is required");
            }
            if (referenceNumber == null || referenceNumber.isBlank()) {
                throw new IllegalStateException("referenceNumber is required");
            }
            if (status == null || status.isBlank()) {
                throw new IllegalStateException("status is required");
            }
            return new Visit(this);
        }
    }
}
