package com.knockknock.backend.service;

import com.knockknock.backend.config.SupabaseConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Concrete adapter that talks to Supabase Storage using HTTP.
 */
@Service
public class SupabaseStorageAdapter implements StorageClient {

    private final SupabaseConfig supabaseConfig;
    private final OkHttpClient httpClient;

    public SupabaseStorageAdapter(SupabaseConfig supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
        this.httpClient = new OkHttpClient();
    }

    @Override
    public String uploadFile(MultipartFile file, String folderPath) throws IOException {
        if (file == null) {
            throw new IOException("File is null");
        }
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        System.out.println("[SupabaseStorageAdapter] Starting upload - fileName: " + file.getOriginalFilename() + ", size: " + file.getSize() + ", folderPath: " + folderPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        String fullPath = folderPath + "/" + fileName;

        try {
            String url = String.format("%s/storage/v1/object/%s/%s",
                    supabaseConfig.getSupabaseUrl(),
                    supabaseConfig.getStorageBucket(),
                    fullPath);

            System.out.println("[SupabaseStorageAdapter] Upload URL: " + url);
            System.out.println("[SupabaseStorageAdapter] Content-Type: " + file.getContentType());

            byte[] fileBytes = file.getBytes();
            System.out.println("[SupabaseStorageAdapter] File bytes read: " + fileBytes.length + " bytes");

            RequestBody body = RequestBody.create(fileBytes, MediaType.parse(file.getContentType()));
            System.out.println("[SupabaseStorageAdapter] RequestBody created");

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                    .header("Content-Type", file.getContentType())
                    .post(body)
                    .build();

            System.out.println("[SupabaseStorageAdapter] Sending request to Supabase...");
            try (Response response = httpClient.newCall(request).execute()) {
                System.out.println("[SupabaseStorageAdapter] Upload response code: " + response.code());
                String responseBody = response.body() != null ? response.body().string() : "Empty response";
                System.out.println("[SupabaseStorageAdapter] Response body: " + responseBody);

                if (!response.isSuccessful() && response.code() != 200 && response.code() != 201) {
                    System.err.println("[SupabaseStorageAdapter] Upload FAILED with code: " + response.code());
                    throw new IOException("Upload failed with code: " + response.code() + ", body: " + responseBody);
                }
            }

            String publicUrl = String.format("%s/storage/v1/object/public/%s/%s",
                    supabaseConfig.getSupabaseUrl(),
                    supabaseConfig.getStorageBucket(),
                    fullPath);

            System.out.println("[SupabaseStorageAdapter] SUCCESS - File uploaded to: " + publicUrl);
            return publicUrl;
        } catch (Exception e) {
            System.err.println("[SupabaseStorageAdapter] CRITICAL ERROR uploading file: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload file to Supabase Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        try {
            System.out.println("[SupabaseStorageAdapter] Deleting file from Supabase: " + filePath);

            String url = String.format("%s/storage/v1/object/%s/%s",
                    supabaseConfig.getSupabaseUrl(),
                    supabaseConfig.getStorageBucket(),
                    filePath);

            System.out.println("[SupabaseStorageAdapter] Delete URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                    .delete()
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                System.out.println("[SupabaseStorageAdapter] Delete response code: " + response.code());
                String responseBody = response.body() != null ? response.body().string() : "Empty response";
                System.out.println("[SupabaseStorageAdapter] Delete response body: " + responseBody);

                if (response.isSuccessful() || response.code() == 204 || response.code() == 200) {
                    System.out.println("[SupabaseStorageAdapter] SUCCESS - File deleted from Supabase: " + filePath);
                } else {
                    System.err.println("[SupabaseStorageAdapter] Delete failed with code: " + response.code());
                    throw new IOException("Delete failed with code: " + response.code() + ", body: " + responseBody);
                }
            }
        } catch (Exception e) {
            System.err.println("[SupabaseStorageAdapter] Error deleting file from Supabase: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to delete file from Supabase Storage: " + e.getMessage(), e);
        }
    }
}
