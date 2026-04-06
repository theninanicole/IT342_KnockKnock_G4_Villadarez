package com.knockknock.backend.service;

import com.knockknock.backend.entity.Visit;
import com.knockknock.backend.entity.User;
import com.knockknock.backend.entity.Condo;
import com.knockknock.backend.entity.VisitStatusHistory;
import com.knockknock.backend.entity.Notification;
import com.knockknock.backend.dto.UpdateVisitRequest;
import com.knockknock.backend.repository.VisitRepository;
import com.knockknock.backend.repository.UserRepository;
import com.knockknock.backend.repository.CondoRepository;
import com.knockknock.backend.repository.VisitStatusHistoryRepository;
import com.knockknock.backend.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final CondoRepository condoRepository;
    private final VisitStatusHistoryRepository visitStatusHistoryRepository;
    private final NotificationRepository notificationRepository;

    public VisitServiceImpl(VisitRepository visitRepository,
                            UserRepository userRepository,
                            CondoRepository condoRepository,
                            VisitStatusHistoryRepository visitStatusHistoryRepository,
                            NotificationRepository notificationRepository) {
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.condoRepository = condoRepository;
        this.visitStatusHistoryRepository = visitStatusHistoryRepository;
        this.notificationRepository = notificationRepository;
    }

    private void createVisitNotification(Visit visit, String eventType) {
        if (visit == null || visit.getVisitor() == null) {
            return;
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
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
            formattedDate = date.format(formatter);
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
                return;
        }

        Notification notification = new Notification(
                visit.getVisitor(),
                type,
                title,
                message
        );
        notificationRepository.save(notification);
    }

    private void applyMissedStatusIfPast(Visit visit) {
        if (visit == null) {
            return;
        }

        LocalDate visitDate = visit.getVisitDate();
        String status = visit.getStatus();

        if (visitDate == null || status == null) {
            return;
        }

        boolean isScheduled = "SCHEDULED".equalsIgnoreCase(status);
        boolean isPast = visitDate.isBefore(LocalDate.now());
        boolean notCheckedIn = visit.getCheckInTime() == null;

        if (isScheduled && isPast && notCheckedIn) {
            visit.setStatus("MISSED");
            Visit saved = visitRepository.save(visit);

            // Notify visitor about missed visit
            createVisitNotification(saved, "missed");
        }
    }

    private void recordStatusChange(Visit visit, String previousStatus, String newStatus,
                                    String modifiedByName, String modifiedByRole) {
        if (visit == null || newStatus == null) {
            return;
        }

        VisitStatusHistory history = new VisitStatusHistory();
        history.setVisit(visit);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setModifiedByName(modifiedByName);
        history.setModifiedByRole(modifiedByRole);

        visitStatusHistoryRepository.save(history);
    }

    @Override
    public Visit createVisit(UUID visitorId, UUID condoId, String unitNumber, String dateOfVisit, 
                            String purposeOfVisit) {
        User visitor = userRepository.findById(visitorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visitor not found"));

        Condo condo = condoRepository.findById(condoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Condominium not found"));

        // Parse the date string to LocalDate (format: YYYY-MM-DD)
        LocalDate parsedDate = LocalDate.parse(dateOfVisit);

        // Generate reference number with condo code
        String referenceNumber = generateReferenceNumber(condo.getCode());

        Visit visit = new Visit(visitor, condo, referenceNumber, unitNumber, purposeOfVisit, parsedDate, "SCHEDULED");
        Visit saved = visitRepository.save(visit);

        // Notify visitor about scheduled visit
        createVisitNotification(saved, "scheduled");

        return saved;
    }

    @Override
    public Visit updateVisit(UUID visitId, UpdateVisitRequest request) {
        System.out.println("[VisitService] Updating visit: " + visitId);
        
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        // First, update status to MISSED if this is a past, unchecked visit
        applyMissedStatusIfPast(visit);

        // Only allow editing if visit is still SCHEDULED and not in the past
        if (!"SCHEDULED".equalsIgnoreCase(visit.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only scheduled visits can be edited");
        }

        if (visit.getVisitDate() != null && visit.getVisitDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit a visit that is in the past");
        }

        // Validate and update unitNumber
        if (request.getUnitNumber() != null && !request.getUnitNumber().trim().isEmpty()) {
            visit.setUnitNumber(request.getUnitNumber().trim());
            System.out.println("[VisitService] Updated unitNumber to: " + request.getUnitNumber());
        }

        // Validate and update purpose
        if (request.getPurpose() != null && !request.getPurpose().trim().isEmpty()) {
            visit.setPurpose(request.getPurpose().trim());
            System.out.println("[VisitService] Updated purpose to: " + request.getPurpose());
        }

        // Validate and update visitDate
        if (request.getVisitDate() != null && !request.getVisitDate().trim().isEmpty()) {
            LocalDate parsedDate = LocalDate.parse(request.getVisitDate());

            if (parsedDate.isBefore(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit date cannot be in the past");
            }

            visit.setVisitDate(parsedDate);
            System.out.println("[VisitService] Updated visitDate to: " + request.getVisitDate());
        }

        Visit updatedVisit = visitRepository.save(visit);
        System.out.println("[VisitService] Visit updated successfully");
        return updatedVisit;
    }

    @Override
    public String generateReferenceNumber(String condoCode) {
        // Format: KK-[CONDO_CODE]-YYYY-###### where ###### is a 6-digit sequential number
        int year = java.time.Year.now().getValue();
        int sequentialNumber = generateSequentialNumber();
        return String.format("KK-%s-%d-%06d", condoCode, year, sequentialNumber);
    }

    private int generateSequentialNumber() {
        // Generate a 6-digit sequential number (000000-999999)
        java.util.Random random = new java.util.Random();
        return random.nextInt(1000000);
    }

    @Override
    public List<Visit> getVisitsByUser(UUID userId) {
        System.out.println("[VisitService] getVisitsByUser called with userId: " + userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        System.out.println("[VisitService] Found user: " + user.getEmail());
        List<Visit> visits = visitRepository.findByVisitor(user);

        // Ensure any past, unchecked scheduled visits are marked as MISSED
        for (Visit visit : visits) {
            applyMissedStatusIfPast(visit);
        }

        System.out.println("[VisitService] Query returned " + visits.size() + " visits");
        return visits;
    }

    @Override
    public List<Visit> getVisitsByUserOrderedByDateDesc(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Visit> visits = visitRepository.findByVisitorOrderByVisitDateDesc(user);

        for (Visit visit : visits) {
            applyMissedStatusIfPast(visit);
        }

        return visits;
    }

    @Override
    public List<Visit> getVisitsByCondo(UUID condoId) {
        List<Visit> visits = visitRepository.findByCondo_CondoId(condoId);

        // Ensure any past, unchecked scheduled visits are marked as MISSED
        for (Visit visit : visits) {
            applyMissedStatusIfPast(visit);
        }

        return visits;
    }

    @Override
    public List<Visit> getAdminVisits(UUID condoId, String statusFilter) {
        List<Visit> visits;

        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            String normalizedStatus = statusFilter.trim().toUpperCase();
            visits = visitRepository.findByCondo_CondoIdAndStatusIgnoreCaseOrderByVisitDateDesc(condoId, normalizedStatus);
        } else {
            visits = visitRepository.findByCondo_CondoIdOrderByVisitDateDesc(condoId);
        }

        for (Visit visit : visits) {
            applyMissedStatusIfPast(visit);
        }

        return visits;
    }

    @Override
    public Visit getVisitById(UUID visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        applyMissedStatusIfPast(visit);
        return visit;
    }

    @Override
    public Visit findByReferenceNumber(String referenceNumber) {
        Visit visit = visitRepository.findByReferenceNumber(referenceNumber)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        applyMissedStatusIfPast(visit);
        return visit;
    }

    @Override
    public Visit checkInVisit(UUID visitId, UUID adminCondoId, String modifiedByName, String modifiedByRole) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        String previousStatus = visit.getStatus();

        if (!"SCHEDULED".equalsIgnoreCase(visit.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit is not in SCHEDULED status");
        }

        if (visit.getCondo() == null || visit.getCondo().getCondoId() == null ||
                !visit.getCondo().getCondoId().equals(adminCondoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Visit does not belong to this condominium");
        }

        visit.setStatus("CHECKED-IN");
        visit.setCheckInTime(LocalDateTime.now());
        Visit saved = visitRepository.save(visit);

        recordStatusChange(saved, previousStatus, saved.getStatus(), modifiedByName, modifiedByRole);

        // Notify visitor about check-in
        createVisitNotification(saved, "check_in");

        return saved;
    }

    @Override
    public Visit checkOutVisit(UUID visitId, UUID adminCondoId, String modifiedByName, String modifiedByRole) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Visit not found"));

        String previousStatus = visit.getStatus();

        if (!"CHECKED-IN".equalsIgnoreCase(visit.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visit is not in CHECKED-IN status");
        }

        if (visit.getCondo() == null || visit.getCondo().getCondoId() == null ||
                !visit.getCondo().getCondoId().equals(adminCondoId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Visit does not belong to this condominium");
        }

        visit.setStatus("CHECKED-OUT");
        visit.setCheckOutTime(LocalDateTime.now());
        Visit saved = visitRepository.save(visit);

        recordStatusChange(saved, previousStatus, saved.getStatus(), modifiedByName, modifiedByRole);

        // Notify visitor about check-out
        createVisitNotification(saved, "check_out");

        return saved;
    }
}
