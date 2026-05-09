package edu.cit.villadarez.knockknock.features.notification;

import edu.cit.villadarez.knockknock.features.notification.Notification;
import edu.cit.villadarez.knockknock.features.user.User;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    List<Notification> getUserNotifications(User user, Boolean isRead, int limit);

    void markAsRead(User user, UUID notifId);
}
