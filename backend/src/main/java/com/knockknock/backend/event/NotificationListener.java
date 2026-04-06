package com.knockknock.backend.event;

import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.entity.Visit;
import com.knockknock.backend.entity.VisitStatusHistory;
import com.knockknock.backend.repository.NotificationRepository;
import com.knockknock.backend.repository.VisitStatusHistoryRepository;
import com.knockknock.backend.service.NotificationFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Observer that reacts to visit status changes.
 * It logs the status history and sends notifications.
 */
@Component
public class NotificationListener {

    private final VisitStatusHistoryRepository visitStatusHistoryRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationFactory notificationFactory;

    public NotificationListener(VisitStatusHistoryRepository visitStatusHistoryRepository,
                                NotificationRepository notificationRepository,
                                NotificationFactory notificationFactory) {
        this.visitStatusHistoryRepository = visitStatusHistoryRepository;
        this.notificationRepository = notificationRepository;
        this.notificationFactory = notificationFactory;
    }

    @EventListener
    public void onVisitStatusChanged(VisitStatusChangedEvent event) {
        Visit visit = event.getVisit();

        // 1. Record status change history
        VisitStatusHistory history = new VisitStatusHistory();
        history.setVisit(visit);
        history.setPreviousStatus(event.getPreviousStatus());
        history.setNewStatus(event.getNewStatus());
        history.setModifiedByName(event.getModifiedByName());
        history.setModifiedByRole(event.getModifiedByRole());
        history.setChangedAt(LocalDateTime.now());
        visitStatusHistoryRepository.save(history);

        // 2. Send notification to visitor
        Notification notification = notificationFactory.createVisitNotification(visit, event.getEventType());
        if (notification != null) {
            notificationRepository.save(notification);
        }
    }
}
