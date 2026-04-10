package com.knockknock.backend.dto;

public class FileUploadRequest {
    private String visitId;
    private String filePath;
    private String fileUrl;
    private String fileName;
    private String fileType;

    public FileUploadRequest() {}

    public FileUploadRequest(String visitId, String filePath, String fileUrl, String fileName, String fileType) {
        this.visitId = visitId;
        this.filePath = filePath;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.fileType = fileType;
    }

    public String getVisitId() {
        return visitId;
    }

    public void setVisitId(String visitId) {
        this.visitId = visitId;
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
}
