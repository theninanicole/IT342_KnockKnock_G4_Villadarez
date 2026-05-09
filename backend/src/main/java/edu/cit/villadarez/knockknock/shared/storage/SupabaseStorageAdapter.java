package edu.cit.villadarez.knockknock.shared.storage;

import edu.cit.villadarez.knockknock.config.SupabaseConfig;
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
    public StorageUploadResult uploadFile(MultipartFile file, String folderPath) throws IOException {
        if (file == null) {
            throw new IOException("File is null");
        }
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        System.out.println("[SupabaseStorageAdapter] Starting upload - fileName: " + file.getOriginalFilename() + ", size: " + file.getSize() + ", folderPath: " + folderPath);

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String fileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
        String fullPath = sanitizeFolderPath(folderPath) + "/" + fileName;

        try {
            String url = String.format("%s/storage/v1/object/%s/%s",
                    normalizedSupabaseUrl(),
                    supabaseConfig.getStorageBucket(),
                    fullPath);

            System.out.println("[SupabaseStorageAdapter] Upload URL: " + url);
            System.out.println("[SupabaseStorageAdapter] Content-Type: " + contentType);

            byte[] fileBytes = file.getBytes();
            System.out.println("[SupabaseStorageAdapter] File bytes read: " + fileBytes.length + " bytes");

            RequestBody body = RequestBody.create(fileBytes, MediaType.parse(contentType));
            System.out.println("[SupabaseStorageAdapter] RequestBody created");

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", supabaseConfig.getSupabaseKey())
                    .header("Authorization", "Bearer " + supabaseConfig.getSupabaseKey())
                    .header("Content-Type", contentType)
                    .header("Cache-Control", "3600")
                    .header("x-upsert", "false")
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
                    normalizedSupabaseUrl(),
                    supabaseConfig.getStorageBucket(),
                    fullPath);

            System.out.println("[SupabaseStorageAdapter] SUCCESS - File uploaded to: " + publicUrl);
            return new StorageUploadResult(fullPath, publicUrl);
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
                    normalizedSupabaseUrl(),
                    supabaseConfig.getStorageBucket(),
                    sanitizeFolderPath(filePath));

            System.out.println("[SupabaseStorageAdapter] Delete URL: " + url);

            Request request = new Request.Builder()
                    .url(url)
                    .header("apikey", supabaseConfig.getSupabaseKey())
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

    private String normalizedSupabaseUrl() throws IOException {
        String supabaseUrl = supabaseConfig.getSupabaseUrl();
        if (supabaseUrl == null || supabaseUrl.isBlank()) {
            throw new IOException("Supabase URL is not configured");
        }

        String key = supabaseConfig.getSupabaseKey();
        if (key == null || key.isBlank()) {
            throw new IOException("Supabase key is not configured");
        }

        return supabaseUrl.replaceAll("/+$", "");
    }

    private String sanitizeFolderPath(String folderPath) throws IOException {
        if (folderPath == null || folderPath.isBlank()) {
            throw new IOException("Storage folder path is required");
        }

        String safePath = folderPath
                .replace("\\", "/")
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .replaceAll("[^a-zA-Z0-9/_-]", "_")
                .replaceAll("/{2,}", "/");

        if (safePath.isBlank()) {
            throw new IOException("Storage folder path is invalid");
        }

        return safePath;
    }

    private String sanitizeFileName(String originalFileName) {
        String fallbackName = "visitor_id";
        String name = originalFileName == null || originalFileName.isBlank()
                ? fallbackName
                : originalFileName.substring(originalFileName.lastIndexOf('/') + 1);

        name = name.substring(name.lastIndexOf('\\') + 1)
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        return name.isBlank() ? fallbackName : name;
    }
}
