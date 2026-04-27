package edu.cit.villadarez.knockknock.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * High-level service used by controllers and other services.
 * Delegates actual storage operations to a StorageClient adapter.
 */
@Service
public class FileStorageService {

    private final StorageClient storageClient;

    public FileStorageService(StorageClient storageClient) {
        this.storageClient = storageClient;
    }

    /**
     * Upload a file using the configured storage client and return the public URL.
     */
    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        System.out.println("[FileStorageService] Delegating upload to StorageClient");
        return storageClient.uploadFile(file, folderPath);
    }

    /**
     * Delete a file using the configured storage client.
     */
    public void deleteFile(String filePath) {
        System.out.println("[FileStorageService] Delegating delete to StorageClient");
        try {
            storageClient.deleteFile(filePath);
        } catch (IOException e) {
            System.err.println("[FileStorageService] Error deleting file via StorageClient: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
