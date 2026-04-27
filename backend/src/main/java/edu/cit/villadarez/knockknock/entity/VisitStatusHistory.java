package edu.cit.villadarez.knockknock.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "visit_status_history")
public class VisitStatusHistory {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "history_id", columnDefinition = "UUID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", referencedColumnName = "visit_id", nullable = false)
    @JsonIgnore
    private Visit visit;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "new_status", nullable = false)
    private String newStatus;

    @Column(name = "modified_by_name")
    private String modifiedByName;

    @Column(name = "modified_by_role")
    private String modifiedByRole;

    @CreationTimestamp
    @Column(name = "changed_at", updatable = false)
    private LocalDateTime changedAt;

    public VisitStatusHistory() {
    }

    public VisitStatusHistory(Visit visit, String previousStatus, String newStatus,
                              String modifiedByName, String modifiedByRole) {
        this.visit = visit;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.modifiedByName = modifiedByName;
        this.modifiedByRole = modifiedByRole;
    }

    public UUID getId() {
        return id;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getModifiedByName() {
        return modifiedByName;
    }

    public void setModifiedByName(String modifiedByName) {
        this.modifiedByName = modifiedByName;
    }

    public String getModifiedByRole() {
        return modifiedByRole;
    }

    public void setModifiedByRole(String modifiedByRole) {
        this.modifiedByRole = modifiedByRole;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
