package com.knockknock.backend.event;

import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.entity.Visit;
import com.knockknock.backend.entity.VisitStatusHistory;
import com.knockknock.backend.repository.NotificationRepository;
import com.knockknock.backend.repository.VisitStatusHistoryRepository;
import com.knockknock.backend.service.NotificationFactory;
import com.knockknock.backend.service.EmailService;
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
    private final EmailService emailService;

    public NotificationListener(VisitStatusHistoryRepository visitStatusHistoryRepository,
                                NotificationRepository notificationRepository,
                                NotificationFactory notificationFactory,
                                EmailService emailService) {
        this.visitStatusHistoryRepository = visitStatusHistoryRepository;
        this.notificationRepository = notificationRepository;
        this.notificationFactory = notificationFactory;
        this.emailService = emailService;
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

        // 3. Send plain visit-created email (no QR) when a visit is scheduled
        if ("scheduled".equalsIgnoreCase(event.getEventType()) && visit != null && visit.getVisitor() != null) {
            String toEmail = visit.getVisitor().getEmail();
            String visitorName = visit.getVisitor().getFullName();
            String refNumber = visit.getReferenceNumber();
            String condoName = visit.getCondo() != null ? visit.getCondo().getName() : null;
            String visitDate = visit.getVisitDate() != null ? visit.getVisitDate().toString() : null;

            if (toEmail != null && !toEmail.isBlank()) {
                emailService.sendVisitCreatedEmail(toEmail, visitorName, refNumber, visitDate, condoName);
            }
        }
    }
}
