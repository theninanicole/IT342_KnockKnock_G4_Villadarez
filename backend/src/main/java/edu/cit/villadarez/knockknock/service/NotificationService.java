package edu.cit.villadarez.knockknock.service;

import edu.cit.villadarez.knockknock.entity.Notification;
import edu.cit.villadarez.knockknock.entity.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<Notification> getUserNotifications(User user, Boolean isRead, int limit);

    void markAsRead(User user, UUID notifId);
}
