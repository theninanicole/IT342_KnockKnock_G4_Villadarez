package com.knockknock.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "visit_files")
public class VisitFile {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "file_id", columnDefinition = "UUID")
    private UUID fileId;

    @ManyToOne
    @JoinColumn(name = "visit_id", referencedColumnName = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "file_url", nullable = true, length = 1000)
    private String fileUrl;

    @Column(name = "file_name", nullable = true)
    private String fileName;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    // Constructors
    public VisitFile() {}

    public VisitFile(Visit visit, String filePath, String fileType) {
        this.visit = visit;
        this.filePath = filePath;
        this.fileType = fileType;
    }

    public VisitFile(Visit visit, String filePath, String fileUrl, String fileName, String fileType) {
        this.visit = visit;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    // Getters and Setters
    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
