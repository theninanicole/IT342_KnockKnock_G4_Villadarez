package edu.cit.villadarez.knockknock.event;

import edu.cit.villadarez.knockknock.entity.Visit;

/**
 * Domain event published whenever a visit's status changes.
 */
public class VisitStatusChangedEvent {

    private final Visit visit;
    private final String previousStatus;
    private final String newStatus;
    private final String modifiedByName;
    private final String modifiedByRole;
    private final String eventType; // e.g. "check_in", "check_out", "missed", "scheduled"

    public VisitStatusChangedEvent(Visit visit,
                                   String previousStatus,
                                   String newStatus,
                                   String modifiedByName,
                                   String modifiedByRole,
                                   String eventType) {
        this.visit = visit;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.modifiedByName = modifiedByName;
        this.modifiedByRole = modifiedByRole;
        this.eventType = eventType;
    }

    public Visit getVisit() {
        return visit;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public String getModifiedByName() {
        return modifiedByName;
    }

    public String getModifiedByRole() {
        return modifiedByRole;
    }

    public String getEventType() {
        return eventType;
    }
}
