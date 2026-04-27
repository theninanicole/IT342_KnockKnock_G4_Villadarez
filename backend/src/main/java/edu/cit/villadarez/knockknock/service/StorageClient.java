package edu.cit.villadarez.knockknock.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Adapter interface for file storage providers.
 */
public interface StorageClient {

    /**
     * Upload a file to the underlying storage provider and return a public URL.
     */
    String uploadFile(MultipartFile file, String folderPath) throws IOException;

    /**
     * Delete a file from the underlying storage provider.
     */
    void deleteFile(String filePath) throws IOException;
}
