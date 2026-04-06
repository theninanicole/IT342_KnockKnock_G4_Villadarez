package com.knockknock.backend.service;

import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.entity.User;
import com.knockknock.backend.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<Notification> getUserNotifications(User user, Boolean isRead, int limit) {
        int pageSize = (limit <= 0) ? 20 : limit;
        Pageable pageable = PageRequest.of(0, pageSize);

        if (isRead != null) {
            return notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, isRead, pageable);
        }

        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    public void markAsRead(User user, UUID notifId) {
        Notification notification = notificationRepository.findByNotifIdAndUser(notifId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }
}
