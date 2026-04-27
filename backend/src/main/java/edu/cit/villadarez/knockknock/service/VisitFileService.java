package edu.cit.villadarez.knockknock.service;

import edu.cit.villadarez.knockknock.dto.FileUploadRequest;
import edu.cit.villadarez.knockknock.dto.VisitFileDTO;
import edu.cit.villadarez.knockknock.entity.Visit;
import edu.cit.villadarez.knockknock.entity.VisitFile;
import edu.cit.villadarez.knockknock.repository.VisitRepository;
import edu.cit.villadarez.knockknock.repository.VisitFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class VisitFileService {

    private final VisitFileRepository visitFileRepository;
    private final VisitRepository visitRepository;
    private final FileStorageService fileStorageService;

    public VisitFileService(VisitFileRepository visitFileRepository, VisitRepository visitRepository, FileStorageService fileStorageService) {
        this.visitFileRepository = visitFileRepository;
        this.visitRepository = visitRepository;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Save file metadata from frontend after Supabase direct upload
     * @param request - FileUploadRequest containing file metadata
     * @return Saved VisitFile entity
     */
    public VisitFile saveFileMetadata(FileUploadRequest request) {
        System.out.println("[VisitFileService] Saving file metadata for visit: " + request.getVisitId());

        // Validate visit exists
        UUID visitId = UUID.fromString(request.getVisitId());
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        System.out.println("[VisitFileService] Visit found: " + visit.getReferenceNumber());

        // Create and save VisitFile entity
        VisitFile visitFile = new VisitFile(
                visit,
                request.getFilePath(),
                request.getFileUrl(),
                request.getFileName(),
                request.getFileType()
        );

        VisitFile saved = visitFileRepository.save(visitFile);
        System.out.println("[VisitFileService] File metadata saved with ID: " + saved.getFileId());
        System.out.println("[VisitFileService] File URL: " + saved.getFileUrl());
        System.out.println("[VisitFileService] File Name: " + saved.getFileName());

        return saved;
    }

    /**
     * Get file names only for a visit - fetches directly from visit_files table
     */
    public List<VisitFileDTO> getFileNamesByVisit(UUID visitId) {
        System.out.println("[VisitFileService] Fetching file names for visit: " + visitId);

        // Validate visit exists
        visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        return visitFileRepository.findFileNamesByVisitId(visitId);
    }

    /**
     * Get all files for a visit
     */
    public List<VisitFile> getFilesByVisit(UUID visitId) {
        System.out.println("[VisitFileService] Fetching files for visit: " + visitId);

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        return visit.getVisitFiles();
    }

    /**
     * Delete file metadata. The Supabase Storage object is deleted
     * from the frontend using supabase-js; this service now only
     * removes the corresponding row from visit_files.
     */
    @Transactional
    public void deleteFileMetadata(UUID fileId) {
        System.out.println("[VisitFileService] Starting deletion for file: " + fileId);

        try {
            VisitFile visitFile = visitFileRepository.findById(fileId)
                    .orElseThrow(() -> {
                        String msg = "File not found: " + fileId;
                        System.err.println("[VisitFileService] " + msg);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, msg);
                    });

            System.out.println("[VisitFileService] Found file in database - Path: " + visitFile.getFilePath());

            // Frontend is responsible for deleting from Supabase Storage.
            // Here we only delete the metadata row from the database.
            System.out.println("[VisitFileService] Deleting metadata from database - File ID: " + fileId);
            visitFileRepository.delete(visitFile);
            System.out.println("[VisitFileService] File successfully deleted from database");
            System.out.println("[VisitFileService] Deletion complete for file: " + fileId);
        } catch (Exception e) {
            System.err.println("[VisitFileService] Unexpected error during deletion: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Get file by ID
     */
    public VisitFile getFileById(UUID fileId) {
        return visitFileRepository.findById(fileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));
    }
}

