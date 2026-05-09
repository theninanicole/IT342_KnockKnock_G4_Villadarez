package edu.cit.villadarez.knockknock.features.visit;

import edu.cit.villadarez.knockknock.features.visit.VisitStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VisitStatusHistoryRepository extends JpaRepository<VisitStatusHistory, UUID> {
    List<VisitStatusHistory> findByVisit_Condo_CondoIdOrderByChangedAtDesc(UUID condoId);
}
