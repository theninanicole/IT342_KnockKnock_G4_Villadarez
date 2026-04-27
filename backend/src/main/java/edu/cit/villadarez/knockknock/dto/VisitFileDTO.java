package edu.cit.villadarez.knockknock.dto;

import java.util.UUID;

public class VisitFileDTO {
    private UUID fileId;
    private String fileName;
    private String fileUrl;
    private String filePath;

    public VisitFileDTO() {}

    public VisitFileDTO(UUID fileId, String fileName, String fileUrl) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
    }

    public VisitFileDTO(UUID fileId, String fileName, String fileUrl, String filePath) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
    }

    public UUID getFileId() {
        return fileId;
    }

    public void setFileId(UUID fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
