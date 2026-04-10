package com.knockknock.backend.service;

import com.knockknock.backend.entity.Visit;
import com.knockknock.backend.entity.User;
import com.knockknock.backend.entity.Condo;
import com.knockknock.backend.event.VisitStatusChangedEvent;
import com.knockknock.backend.dto.UpdateVisitRequest;
import com.knockknock.backend.repository.VisitRepository;
import com.knockknock.backend.repository.UserRepository;
import com.knockknock.backend.repository.CondoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class VisitServiceImpl implements VisitService {

    private final VisitRepository visitRepository;
    private final UserRepository userRepository;
    private final CondoRepository condoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public VisitServiceImpl(VisitRepository visitRepository,
                            UserRepository userRepository,
                            CondoRepository condoRepository,
                            ApplicationEventPublisher eventPublisher) {
        this.visitRepository = visitRepository;
        this.userRepository = userRepository;
        this.condoRepository = condoRepository;
        this.eventPublisher = eventPublisher;
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
            String previousStatus = visit.getStatus();
            visit.setStatus("MISSED");
            Visit saved = visitRepository.save(visit);

            eventPublisher.publishEvent(new VisitStatusChangedEvent(
                    saved,
                    previousStatus,
                    saved.getStatus(),
                    null,
                    null,
                    "missed"));
        }
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

        Visit visit = Visit.builder()
            .visitor(visitor)
            .condo(condo)
            .referenceNumber(referenceNumber)
            .unitNumber(unitNumber)
            .purpose(purposeOfVisit)
            .visitDate(parsedDate)
            .status("SCHEDULED")
            .build();
        Visit saved = visitRepository.save(visit);

        // Publish domain event; observers handle history + notifications
        eventPublisher.publishEvent(new VisitStatusChangedEvent(
                saved,
                null,
                saved.getStatus(),
                null,
                null,
                "scheduled"));

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

        // Publish domain event; observers handle history + notifications
        eventPublisher.publishEvent(new VisitStatusChangedEvent(
            saved,
            previousStatus,
            saved.getStatus(),
            modifiedByName,
            modifiedByRole,
            "check_in"));

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

        // Publish domain event; observers handle history + notifications
        eventPublisher.publishEvent(new VisitStatusChangedEvent(
            saved,
            previousStatus,
            saved.getStatus(),
            modifiedByName,
            modifiedByRole,
            "check_out"));

        return saved;
    }
}
