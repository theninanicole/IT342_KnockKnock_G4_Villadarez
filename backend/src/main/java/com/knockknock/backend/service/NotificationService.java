package com.knockknock.backend.service;

import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<Notification> getUserNotifications(User user, Boolean isRead, int limit);

    void markAsRead(User user, UUID notifId);
}
