package edu.cit.villadarez.knockknock.event;

import edu.cit.villadarez.knockknock.entity.Notification;
import edu.cit.villadarez.knockknock.entity.Visit;
import edu.cit.villadarez.knockknock.entity.VisitStatusHistory;
import edu.cit.villadarez.knockknock.repository.NotificationRepository;
import edu.cit.villadarez.knockknock.repository.VisitStatusHistoryRepository;
import edu.cit.villadarez.knockknock.service.NotificationFactory;
import edu.cit.villadarez.knockknock.service.EmailService;
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
