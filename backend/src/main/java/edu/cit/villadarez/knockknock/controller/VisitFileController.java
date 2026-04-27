package edu.cit.villadarez.knockknock.controller;

import edu.cit.villadarez.knockknock.dto.FileUploadRequest;
import edu.cit.villadarez.knockknock.dto.VisitFileDTO;
import edu.cit.villadarez.knockknock.entity.VisitFile;
import edu.cit.villadarez.knockknock.service.VisitFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "http://localhost:5173")
public class VisitFileController {

    private final VisitFileService visitFileService;

    public VisitFileController(VisitFileService visitFileService) {
        this.visitFileService = visitFileService;
    }

    @PostMapping("/{visitId}/files")
    public ResponseEntity<?> saveFileMetadata(@PathVariable String visitId, @RequestBody FileUploadRequest request) {
        try {
            System.out.println("[POST /visits/{visitId}/files] Saving file metadata");
            System.out.println("[POST /visits/{visitId}/files] Path visitId: " + visitId);
            System.out.println("[POST /visits/{visitId}/files] Body Visit ID: " + request.getVisitId());
            System.out.println("[POST /visits/{visitId}/files] File: " + request.getFileName());
            System.out.println("[POST /visits/{visitId}/files] URL: " + request.getFileUrl());

            // Validate request
            if (visitId == null || visitId.isEmpty()) {
                return ResponseEntity.badRequest().body("Visit ID is required");
            }

            // Ensure the DTO has the same visitId so downstream services keep working
            if (request.getVisitId() == null || request.getVisitId().isEmpty()) {
                request.setVisitId(visitId);
            }

            if (request.getFileUrl() == null || request.getFileUrl().isEmpty()) {
                return ResponseEntity.badRequest().body("File URL is required");
            }

            // Save file metadata
            VisitFile visitFile = visitFileService.saveFileMetadata(request);

            System.out.println("[POST /visits/{visitId}/files] File metadata saved successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(visitFile);
        } catch (Exception e) {
            System.err.println("[POST /visits/{visitId}/files] Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save file metadata: " + e.getMessage());
        }
    }

    /**
     * Get all files for a specific visit
     */
    @GetMapping("/{visitId}/files")
    public ResponseEntity<?> getFilesByVisit(@PathVariable String visitId) {
        try {
            System.out.println("[GET /visits/{visitId}/files] Fetching files for visit: " + visitId);

            List<VisitFileDTO> fileDTOs = visitFileService.getFileNamesByVisit(UUID.fromString(visitId));

            System.out.println("[GET /visits/{visitId}/files] Found " + fileDTOs.size() + " files");

            return ResponseEntity.ok(fileDTOs);
        } catch (Exception e) {
            System.err.println("[GET /visits/{visitId}/files] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch files: " + e.getMessage());
        }
    }

    /**
     * Get file by ID
     */
    @GetMapping("/{visitId}/files/{fileId}")
    public ResponseEntity<?> getFile(@PathVariable String visitId, @PathVariable String fileId) {
        try {
            System.out.println("[GET /visits/{visitId}/files/{fileId}] Fetching file: " + fileId + " for visit " + visitId);

            VisitFile file = visitFileService.getFileById(UUID.fromString(fileId));

            return ResponseEntity.ok(file);
        } catch (Exception e) {
            System.err.println("[GET /visits/{visitId}/files/{fileId}] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("File not found: " + e.getMessage());
        }
    }

    /**
     * Delete file metadata
     */
    @DeleteMapping("/{visitId}/files/{fileId}")
    public ResponseEntity<?> deleteFile(@PathVariable String visitId, @PathVariable String fileId) {
        try {
            System.out.println("[VisitFileController] DELETE /visits/" + visitId + "/files/" + fileId);
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
