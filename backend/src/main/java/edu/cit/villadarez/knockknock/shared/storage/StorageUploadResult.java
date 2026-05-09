package edu.cit.villadarez.knockknock.shared.storage;

public class StorageUploadResult {
    private final String filePath;
    private final String publicUrl;

    public StorageUploadResult(String filePath, String publicUrl) {
        this.filePath = filePath;
        this.publicUrl = publicUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getPublicUrl() {
        return publicUrl;
    }
}
