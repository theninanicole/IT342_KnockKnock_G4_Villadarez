package com.knockknock.backend.controller;

import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.entity.User;
import com.knockknock.backend.repository.UserRepository;
import com.knockknock.backend.service.NotificationService;
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

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
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
    public ResponseEntity<?> getNotifications(
            @RequestParam(value = "isRead", required = false) Boolean isRead,
            @RequestParam(value = "limit", required = false, defaultValue = "20") Integer limit
    ) {
        try {
            User currentUser = getCurrentUser();
            List<Notification> notifications = notificationService.getUserNotifications(currentUser, isRead, limit != null ? limit : 20);

            List<Map<String, Object>> notificationDtos = notifications.stream().map(n -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("notifId", n.getNotifId());
                dto.put("type", n.getType());
                dto.put("title", n.getTitle());
                dto.put("message", n.getMessage());
                dto.put("isRead", n.isRead());
                dto.put("createdAt", n.getCreatedAt());
                return dto;
            }).toList();

            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationDtos);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch notifications"));
        }
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable("id") String id) {
        try {
            User currentUser = getCurrentUser();
            notificationService.markAsRead(currentUser, UUID.fromString(id));
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid notification ID");
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update notification"));
        }
    }
}
