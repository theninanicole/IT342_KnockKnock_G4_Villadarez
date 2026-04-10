package com.knockknock.backend.service;

import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.entity.Visit;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Factory Method for creating Notification instances related to Visit events.
 * Centralizes all the "what a notification looks like" logic.
 */
@Component
public class NotificationFactory {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public Notification createVisitNotification(Visit visit, String eventType) {
        if (visit == null || visit.getVisitor() == null) {
            return null;
        }

        String condoName = (visit.getCondo() != null && visit.getCondo().getName() != null)
                ? visit.getCondo().getName()
                : "your condominium";

        String unitNumber = visit.getUnitNumber();
        String unitLabel = (unitNumber != null && !unitNumber.isBlank())
                ? "Unit " + unitNumber
                : "your unit";

        LocalDate date = visit.getVisitDate();
        String formattedDate = null;
        if (date != null) {
            formattedDate = date.format(DATE_FORMATTER);
        }

        String type;
        String title;
        String message;

        switch (eventType) {
            case "check_in":
                type = "check_in";
                title = "Welcome!";
                message = "You've successfully checked into " + unitLabel + " at " + condoName + ". Enjoy your visit!";
                break;
            case "check_out":
                type = "check_out";
                title = "Check-out complete";
                message = "You're all checked out of " + unitLabel + ". Have a great rest of your day!";
                break;
            case "scheduled":
                type = "scheduled";
                title = "Your visit is confirmed!";
                String datePartScheduled = (formattedDate != null) ? formattedDate : "soon";
                message = "You're all set to visit " + unitLabel + " at " + condoName + " on " + datePartScheduled + ".";
                break;
            case "cancelled":
                type = "cancelled";
                title = "Visit cancelled";
                String datePartCancelled = (formattedDate != null) ? formattedDate : "the scheduled date";
                message = "Your scheduled visit to " + unitLabel + " on " + datePartCancelled + " has been cancelled.";
                break;
            case "missed":
                type = "missed";
                title = "Looks like you missed a visit";
                message = "Your scheduled visit to " + unitLabel + " was marked as missed. You can schedule a new one anytime.";
                break;
            default:
                return null;
        }

        return Notification.builder()
            .user(visit.getVisitor())
            .type(type)
            .title(title)
            .message(message)
            .build();
    }
}
