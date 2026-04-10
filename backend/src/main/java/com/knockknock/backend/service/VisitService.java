package com.knockknock.backend.service;

import com.knockknock.backend.entity.Visit;
import com.knockknock.backend.dto.UpdateVisitRequest;
import java.util.List;
import java.util.UUID;

public interface VisitService {
    Visit createVisit(UUID visitorId, UUID condoId, String unitNumber, String visitDate, 
                      String purpose);
    List<Visit> getVisitsByUser(UUID userId);
    List<Visit> getVisitsByCondo(UUID condoId);
    List<Visit> getAdminVisits(UUID condoId, String statusFilter);
    Visit getVisitById(UUID visitId);
    Visit updateVisit(UUID visitId, UpdateVisitRequest request);
    String generateReferenceNumber(String condoCode);
    Visit findByReferenceNumber(String referenceNumber);
    Visit checkInVisit(UUID visitId, UUID adminCondoId, String modifiedByName, String modifiedByRole);
    Visit checkOutVisit(UUID visitId, UUID adminCondoId, String modifiedByName, String modifiedByRole);
    List<Visit> getVisitsByUserOrderedByDateDesc(UUID userId);
    Visit generateQrForVisit(UUID visitId);
}
