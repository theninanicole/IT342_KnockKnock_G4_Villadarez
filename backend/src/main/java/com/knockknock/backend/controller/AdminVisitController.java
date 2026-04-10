package com.knockknock.backend.controller;

import com.knockknock.backend.entity.User;
import com.knockknock.backend.entity.Visit;
import com.knockknock.backend.repository.UserRepository;
import com.knockknock.backend.service.VisitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/visits")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminVisitController {

    private final VisitService visitService;
    private final UserRepository userRepository;

    public AdminVisitController(VisitService visitService, UserRepository userRepository) {
        this.visitService = visitService;
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

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAdminVisits(@RequestParam(value = "status", required = false) String status) {
        User currentUser = getCurrentUser();

        if (currentUser.getRole() == null || !"CONDOMINIUM_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only condominium admins can access visit lists");
        }

        if (currentUser.getCondo() == null || currentUser.getCondo().getCondoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Admin does not have an assigned condominium");
        }

        UUID condoId = currentUser.getCondo().getCondoId();

        String normalizedStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            String value = status.trim().toLowerCase();
            switch (value) {
                case "scheduled":
                    normalizedStatus = "SCHEDULED";
                    break;
                case "checked-in":
                case "checkedin":
                    normalizedStatus = "CHECKED-IN";
                    break;
                case "checked-out":
                case "checkedout":
                    normalizedStatus = "CHECKED-OUT";
                    break;
                case "cancelled":
                    normalizedStatus = "CANCELLED";
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status filter");
            }
        }

        List<Visit> visits = visitService.getAdminVisits(condoId, normalizedStatus);

        List<Map<String, Object>> visitDtos = visits.stream().map(visit -> {
            Map<String, Object> visitorMap = new HashMap<>();
            if (visit.getVisitor() != null) {
                visitorMap.put("visitorId", visit.getVisitor().getId());
                visitorMap.put("fullName", visit.getVisitor().getFullName());
            }

            Map<String, Object> v = new HashMap<>();
            v.put("visitId", visit.getVisitId());
            v.put("referenceNumber", visit.getReferenceNumber());
            v.put("visitDate", visit.getVisitDate());
            v.put("status", visit.getStatus());
            v.put("visitor", visitorMap);
            return v;
        }).collect(Collectors.toList());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("visits", visitDtos);

        return ResponseEntity.ok(responseBody);
    }
}
