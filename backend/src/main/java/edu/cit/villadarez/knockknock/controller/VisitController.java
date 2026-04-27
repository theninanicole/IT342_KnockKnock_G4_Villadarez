package edu.cit.villadarez.knockknock.controller;

import edu.cit.villadarez.knockknock.entity.Visit;
import edu.cit.villadarez.knockknock.entity.VisitFile;
import edu.cit.villadarez.knockknock.entity.User;
import edu.cit.villadarez.knockknock.dto.UpdateVisitRequest;
import edu.cit.villadarez.knockknock.service.VisitService;
import edu.cit.villadarez.knockknock.service.FileStorageService;
import edu.cit.villadarez.knockknock.service.EmailService;
import edu.cit.villadarez.knockknock.repository.UserRepository;
import edu.cit.villadarez.knockknock.repository.VisitFileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "http://localhost:5173")
public class VisitController {

    private final VisitService visitService;
    private final UserRepository userRepository;
    private final VisitFileRepository visitFileRepository;
    private final FileStorageService fileStorageService;
    private final EmailService emailService;

    public VisitController(VisitService visitService, UserRepository userRepository,
                          VisitFileRepository visitFileRepository, FileStorageService fileStorageService,
                          EmailService emailService) {
        this.visitService = visitService;
        this.userRepository = userRepository;
        this.visitFileRepository = visitFileRepository;
        this.fileStorageService = fileStorageService;
        this.emailService = emailService;
    }
    
    @PostMapping("/{visitId}/cancel")
    public ResponseEntity<?> cancelVisit(@PathVariable String visitId) {
        User currentUser = getCurrentUser();
        Visit cancelled = visitService.cancelVisit(UUID.fromString(visitId), currentUser.getId());
        return ResponseEntity.ok(cancelled);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication instanceof UsernamePasswordAuthenticationToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @PostMapping
    public ResponseEntity<?> createVisit(
            @RequestParam String condoId,
            @RequestParam String unitNumber,
            @RequestParam String dateOfVisit,
            @RequestParam String purposeOfVisit,
            @RequestParam(value = "idFile", required = false) MultipartFile idFile) {
        try {
            User currentUser = getCurrentUser();
            System.out.println("[VisitController] Creating visit for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            System.out.println("[VisitController] Condo ID: " + condoId + ", Unit: " + unitNumber + ", Date: " + dateOfVisit);

            // Create visit
            Visit visit = visitService.createVisit(
                    currentUser.getId(),
                    UUID.fromString(condoId),
                    unitNumber,
                    dateOfVisit,
                    purposeOfVisit
            );
            System.out.println("[VisitController] Visit created with ID: " + visit.getVisitId() + ", Reference: " + visit.getReferenceNumber());

            // Send email
            String visitorName = currentUser.getFullName();
            String refNumber = visit.getReferenceNumber();
            String visitDateStr = visit.getVisitDate() != null ? visit.getVisitDate().toString() : "";
            String condoName = visit.getCondo() != null ? visit.getCondo().getName() : "";
            emailService.sendVisitCreatedEmail(
                currentUser.getEmail(),
                visitorName,
                refNumber,
                visitDateStr,
                condoName
            );

            // Save ID file to Supabase Storage if provided
            if (idFile != null && !idFile.isEmpty()) {
                try {
                    String fileUrl = fileStorageService.uploadFile(idFile, "visitors_id");
                    String fileType = idFile.getContentType();
                    VisitFile visitFile = new VisitFile(visit, fileUrl, fileType);
                    visitFileRepository.save(visitFile);
                    System.out.println("[POST /visits] File uploaded to Supabase: " + fileUrl);
                } catch (IOException e) {
                    System.err.println("[POST /visits] Warning: File upload failed, continuing without file: " + e.getMessage());
                    // Don't fail the visit creation if file upload fails
                }
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        } catch (Exception e) {
            System.err.println("[POST /visits] Error creating visit: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create visit: " + e.getMessage());
        }
    }

    @PutMapping("/{visitId}")
    public ResponseEntity<?> updateVisit(
            @PathVariable String visitId,
            @RequestBody UpdateVisitRequest request) {
        try {
            User currentUser = getCurrentUser();
            System.out.println("[VisitController] UPDATE VISIT - visitId: " + visitId);
            System.out.println("[VisitController] Current user: " + currentUser.getEmail());

            // Verify the visit exists and belongs to the current user
            Visit visit = visitService.getVisitById(UUID.fromString(visitId));
            if (!visit.getVisitor().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this visit");
            }

            System.out.println("[VisitController] Updating visit - unitNumber: " + request.getUnitNumber() + ", purpose: " + request.getPurpose() + ", visitDate: " + request.getVisitDate());

            // Update the visit
            Visit updatedVisit = visitService.updateVisit(UUID.fromString(visitId), request);
            System.out.println("[VisitController] Visit updated successfully");

            return ResponseEntity.ok(updatedVisit);
        } catch (ResponseStatusException e) {
            System.err.println("[VisitController] Error: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("[VisitController] Error updating visit: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update visit: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getUserVisits() {
        try {
            User currentUser = getCurrentUser();
            System.out.println("[VisitController] Fetching visits for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");
            List<Visit> visits = visitService.getVisitsByUser(currentUser.getId());
            System.out.println("[VisitController] Found " + visits.size() + " visits");
            for (Visit visit : visits) {
                System.out.println("  - Visit ID: " + visit.getVisitId() + ", Ref: " + visit.getReferenceNumber() + ", Status: " + visit.getStatus());
            }
            return ResponseEntity.ok(visits);
        } catch (Exception e) {
            System.err.println("[VisitController] Error fetching visits: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching visits");
        }
    }

    @GetMapping("/my-visits")
    public ResponseEntity<?> getMyVisits() {
        User currentUser = getCurrentUser();
        System.out.println("[VisitController] Fetching MY visits for user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");

        List<Visit> visits = visitService.getVisitsByUserOrderedByDateDesc(currentUser.getId());

        List<java.util.Map<String, Object>> visitDtos = visits.stream().map(visit -> {
            java.util.Map<String, Object> dto = new java.util.HashMap<>();
            dto.put("id", visit.getVisitId());
            dto.put("referenceNumber", visit.getReferenceNumber());
            dto.put("condoName", visit.getCondo() != null ? visit.getCondo().getName() : null);
            dto.put("unitNumber", visit.getUnitNumber());
            dto.put("visitDate", visit.getVisitDate());
            dto.put("status", visit.getStatus());
            return dto;
        }).toList();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("visits", visitDtos);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{visitId}")
    public ResponseEntity<?> getVisit(@PathVariable String visitId) {
        User currentUser = getCurrentUser();
        Visit visit = visitService.getVisitById(UUID.fromString(visitId));

        if ("VISITOR".equalsIgnoreCase(currentUser.getRole())) {
            if (visit.getVisitor() == null || !visit.getVisitor().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this visit");
            }
        } else if ("CONDOMINIUM_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            if (currentUser.getCondo() == null || currentUser.getCondo().getCondoId() == null ||
                visit.getCondo() == null || visit.getCondo().getCondoId() == null ||
                !visit.getCondo().getCondoId().equals(currentUser.getCondo().getCondoId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Visit does not belong to this condominium");
            }
        }

        return ResponseEntity.ok(visit);
    }

    @GetMapping("/condo/{condoId}")
    public ResponseEntity<?> getCondoVisits(@PathVariable String condoId) {
        List<Visit> visits = visitService.getVisitsByCondo(UUID.fromString(condoId));
        return ResponseEntity.ok(visits);
    }

    @GetMapping("/reference/{referenceNumber}")
    public ResponseEntity<?> getVisitByReference(@PathVariable String referenceNumber) {
        User currentUser = getCurrentUser();

        Visit visit = visitService.findByReferenceNumber(referenceNumber);
        if (visit == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found");
        }

        if ("VISITOR".equalsIgnoreCase(currentUser.getRole())) {
            if (visit.getVisitor() == null || !visit.getVisitor().getId().equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this visit");
            }
        } else if ("CONDOMINIUM_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            if (currentUser.getCondo() == null || currentUser.getCondo().getCondoId() == null ||
                visit.getCondo() == null || visit.getCondo().getCondoId() == null ||
                !visit.getCondo().getCondoId().equals(currentUser.getCondo().getCondoId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Visit does not belong to this condominium");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to view this visit");
        }

        return ResponseEntity.ok(visit);
    }

    @PostMapping("/{visitId}/qr")
    public ResponseEntity<?> generateVisitQr(@PathVariable String visitId) {
        User currentUser = getCurrentUser();

        Visit visit = visitService.getVisitById(UUID.fromString(visitId));
        if (visit.getVisitor() == null || !visit.getVisitor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to generate QR for this visit");
        }

        Visit updated = visitService.generateQrForVisit(UUID.fromString(visitId));
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{visitId}/qr/email")
    public ResponseEntity<?> sendVisitQrEmail(@PathVariable String visitId) {
        User currentUser = getCurrentUser();

        Visit visit = visitService.getVisitById(UUID.fromString(visitId));
        if (visit.getVisitor() == null || !visit.getVisitor().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to email QR for this visit");
        }

        if (!"SCHEDULED".equalsIgnoreCase(visit.getStatus()) && !"CHECKED-IN".equalsIgnoreCase(visit.getStatus())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "QR email is only allowed for scheduled visits.");
        }

        if (visit.getQrImageUrl() == null || visit.getQrImageUrl().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please generate a QR code first.");
        }

        User visitor = visit.getVisitor();
        if (visitor == null || visitor.getEmail() == null || visitor.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No email address found for this visitor.");
        }

        String visitorName = visitor.getFullName() != null ? visitor.getFullName() : "Visitor";
        emailService.sendVisitConfirmationEmail(
                visitor.getEmail(),
                visitorName,
                visit.getReferenceNumber(),
                visit.getQrImageUrl()
        );

        return ResponseEntity.ok().body("QR email has been sent successfully.");
    }

    @PostMapping("/{visitId}/check-in")
    public ResponseEntity<?> checkInVisit(@PathVariable String visitId) {
        User currentUser = getCurrentUser();

        if (!"CONDOMINIUM_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only condominium admins can check in visitors");
        }

        if (currentUser.getCondo() == null || currentUser.getCondo().getCondoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin does not have an assigned condominium");
        }

        Visit updated = visitService.checkInVisit(
                UUID.fromString(visitId),
                currentUser.getCondo().getCondoId(),
                currentUser.getFullName(),
                currentUser.getRole()
        );
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{visitId}/check-out")
    public ResponseEntity<?> checkOutVisit(@PathVariable String visitId) {
        User currentUser = getCurrentUser();

        if (!"CONDOMINIUM_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only condominium admins can check out visitors");
        }

        if (currentUser.getCondo() == null || currentUser.getCondo().getCondoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin does not have an assigned condominium");
        }

        Visit updated = visitService.checkOutVisit(
                UUID.fromString(visitId),
                currentUser.getCondo().getCondoId(),
                currentUser.getFullName(),
                currentUser.getRole()
        );
        return ResponseEntity.ok(updated);
    }
}
