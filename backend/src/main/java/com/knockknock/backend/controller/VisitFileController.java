package com.knockknock.backend.controller;

import com.knockknock.backend.dto.FileUploadRequest;
import com.knockknock.backend.dto.VisitFileDTO;
import com.knockknock.backend.entity.VisitFile;
import com.knockknock.backend.service.VisitFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/visits/files")
@CrossOrigin(origins = "http://localhost:5173")
public class VisitFileController {

    private final VisitFileService visitFileService;

    public VisitFileController(VisitFileService visitFileService) {
        this.visitFileService = visitFileService;
    }

    @PostMapping
    public ResponseEntity<?> saveFileMetadata(@RequestBody FileUploadRequest request) {
        try {
            System.out.println("[POST /visits/files] Saving file metadata");
            System.out.println("[POST /visits/files] Visit ID: " + request.getVisitId());
            System.out.println("[POST /visits/files] File: " + request.getFileName());
            System.out.println("[POST /visits/files] URL: " + request.getFileUrl());

            // Validate request
            if (request.getVisitId() == null || request.getVisitId().isEmpty()) {
                return ResponseEntity.badRequest().body("Visit ID is required");
            }

            if (request.getFileUrl() == null || request.getFileUrl().isEmpty()) {
                return ResponseEntity.badRequest().body("File URL is required");
            }

            // Save file metadata
            VisitFile visitFile = visitFileService.saveFileMetadata(request);

            System.out.println("[POST /visits/files] File metadata saved successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(visitFile);
        } catch (Exception e) {
            System.err.println("[POST /visits/files] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save file metadata: " + e.getMessage());
        }
    }

    /**
     * Get all files for a specific visit
     */
    @GetMapping("/visit/{visitId}")
    public ResponseEntity<?> getFilesByVisit(@PathVariable String visitId) {
        try {
            System.out.println("[GET /visits/files/visit/{visitId}] Fetching files for visit: " + visitId);

            List<VisitFileDTO> fileDTOs = visitFileService.getFileNamesByVisit(UUID.fromString(visitId));

            System.out.println("[GET /visits/files/visit/{visitId}] Found " + fileDTOs.size() + " files");

            return ResponseEntity.ok(fileDTOs);
        } catch (Exception e) {
            System.err.println("[GET /visits/files/visit/{visitId}] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch files: " + e.getMessage());
        }
    }

    /**
     * Get file by ID
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<?> getFile(@PathVariable String fileId) {
        try {
            System.out.println("[GET /visits/files/{fileId}] Fetching file: " + fileId);

            VisitFile file = visitFileService.getFileById(UUID.fromString(fileId));

            return ResponseEntity.ok(file);
        } catch (Exception e) {
            System.err.println("[GET /visits/files/{fileId}] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found: " + e.getMessage());
        }
    }

    /**
     * Delete file metadata
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String fileId) {
        try {
            System.out.println("[VisitFileController] DELETE /visits/files/" + fileId);
            System.out.println("[VisitFileController] Parsing file ID: " + fileId);

            UUID parsedFileId = UUID.fromString(fileId);
            System.out.println("[VisitFileController] Parsed file ID: " + parsedFileId);

            visitFileService.deleteFileMetadata(parsedFileId);

            System.out.println("[VisitFileController] File deleted successfully");
            return ResponseEntity.ok("File deleted successfully");
            
        } catch (IllegalArgumentException e) {
            System.err.println("[VisitFileController] Invalid UUID format: " + fileId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file ID format: " + e.getMessage());
        } catch (ResponseStatusException e) {
            System.err.println("[VisitFileController] ResponseStatusException: " + e.getMessage());
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getReason());
        } catch (Exception e) {
            System.err.println("[VisitFileController] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete file: " + e.getMessage());
        }
    }
}
