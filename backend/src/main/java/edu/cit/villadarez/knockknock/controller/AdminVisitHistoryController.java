package edu.cit.villadarez.knockknock.controller;

import edu.cit.villadarez.knockknock.entity.User;
import edu.cit.villadarez.knockknock.entity.Visit;
import edu.cit.villadarez.knockknock.entity.VisitStatusHistory;
import edu.cit.villadarez.knockknock.repository.UserRepository;
import edu.cit.villadarez.knockknock.repository.VisitStatusHistoryRepository;
import edu.cit.villadarez.knockknock.repository.VisitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/visits-history")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminVisitHistoryController {

    private final VisitStatusHistoryRepository historyRepository;
    private final VisitRepository visitRepository;
    private final UserRepository userRepository;

    public AdminVisitHistoryController(VisitStatusHistoryRepository historyRepository,
                                       VisitRepository visitRepository,
                                       UserRepository userRepository) {
        this.historyRepository = historyRepository;
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
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

    private static class HistoryView {
        private UUID visitId;
        private String visitorName;
        private String referenceNumber;
        private LocalDateTime timestamp;
        private String previousStatus;
        private String newStatus;
        private String modifiedByName;
        private String modifiedByRole;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getVisitStatusHistory() {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == null || !"CONDOMINIUM_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only condominium admins can access visit status history");
        }

        if (currentUser.getCondo() == null || currentUser.getCondo().getCondoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin does not have an assigned condominium");
        }

        UUID condoId = currentUser.getCondo().getCondoId();

        List<HistoryView> combined = new ArrayList<>();
    Set<String> seenKeys = new HashSet<>();

        // 1) Add explicit history rows if present
        List<VisitStatusHistory> historyItems = historyRepository.findByVisit_Condo_CondoIdOrderByChangedAtDesc(condoId);
        for (VisitStatusHistory item : historyItems) {
            HistoryView view = new HistoryView();
            Visit visit = item.getVisit();
            if (visit != null) {
                view.visitId = visit.getVisitId();
                if (visit.getVisitor() != null) {
                    view.visitorName = visit.getVisitor().getFullName();
                }
                view.referenceNumber = visit.getReferenceNumber();
            }
            view.timestamp = item.getChangedAt();
            view.previousStatus = item.getPreviousStatus();
            view.newStatus = item.getNewStatus();
            view.modifiedByName = item.getModifiedByName();
            view.modifiedByRole = item.getModifiedByRole();

                String key = (view.visitId != null ? view.visitId.toString() : "none") + "|" +
                    String.valueOf(view.previousStatus) + "|" +
                    String.valueOf(view.newStatus);
            if (seenKeys.add(key)) {
                combined.add(view);
            }
        }

        // 2) Derive history from existing Visit records (for older data without explicit history)
        List<Visit> visits = visitRepository.findByCondo_CondoId(condoId);
        for (Visit visit : visits) {
            if (visit.getCheckInTime() != null) {
                HistoryView checkIn = new HistoryView();
                checkIn.visitId = visit.getVisitId();
                if (visit.getVisitor() != null) {
                    checkIn.visitorName = visit.getVisitor().getFullName();
                }
                checkIn.referenceNumber = visit.getReferenceNumber();
                checkIn.timestamp = visit.getCheckInTime();
                checkIn.previousStatus = "SCHEDULED";
                checkIn.newStatus = "CHECKED-IN";

                String key = (checkIn.visitId != null ? checkIn.visitId.toString() : "none") + "|" +
                    String.valueOf(checkIn.previousStatus) + "|" +
                    String.valueOf(checkIn.newStatus);
                if (seenKeys.add(key)) {
                    combined.add(checkIn);
                }
            }

            if (visit.getCheckOutTime() != null) {
                HistoryView checkOut = new HistoryView();
                checkOut.visitId = visit.getVisitId();
                if (visit.getVisitor() != null) {
                    checkOut.visitorName = visit.getVisitor().getFullName();
                }
                checkOut.referenceNumber = visit.getReferenceNumber();
                checkOut.timestamp = visit.getCheckOutTime();
                checkOut.previousStatus = "CHECKED-IN";
                checkOut.newStatus = "CHECKED-OUT";

                String key = (checkOut.visitId != null ? checkOut.visitId.toString() : "none") + "|" +
                    String.valueOf(checkOut.previousStatus) + "|" +
                    String.valueOf(checkOut.newStatus);
                if (seenKeys.add(key)) {
                    combined.add(checkOut);
                }
            }
        }

        // Sort newest first
        combined.sort((a, b) -> {
            if (a.timestamp == null && b.timestamp == null) return 0;
            if (a.timestamp == null) return 1;
            if (b.timestamp == null) return -1;
            return b.timestamp.compareTo(a.timestamp);
        });

        List<Map<String, Object>> historyDtos = combined.stream().map(view -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("visitorName", view.visitorName);
            dto.put("referenceNumber", view.referenceNumber);
            dto.put("timestamp", view.timestamp);

            String fromLabel = view.previousStatus != null ? view.previousStatus.toLowerCase() : "";
            String toLabel = view.newStatus != null ? view.newStatus.toLowerCase() : "";
            String transition;
            if (!fromLabel.isEmpty() && !toLabel.isEmpty()) {
                transition = fromLabel + " \u2192 " + toLabel;
            } else {
                transition = (fromLabel + " " + toLabel).trim();
            }
            dto.put("transition", transition);

            if (view.modifiedByName != null && view.modifiedByRole != null) {
                dto.put("modifiedBy", view.modifiedByName + " (" + view.modifiedByRole.toLowerCase() + ")");
            } else if (view.modifiedByName != null) {
                dto.put("modifiedBy", view.modifiedByName);
            } else {
                dto.put("modifiedBy", null);
            }

            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("history", historyDtos);
        return ResponseEntity.ok(responseBody);
    }
}
