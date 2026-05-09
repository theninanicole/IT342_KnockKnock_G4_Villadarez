package edu.cit.villadarez.knockknock.features.visit;

import edu.cit.villadarez.knockknock.features.visit.Visit;
import edu.cit.villadarez.knockknock.features.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisitRepository extends JpaRepository<Visit, UUID> {
    List<Visit> findByVisitor(User visitor);
    List<Visit> findByVisitorOrderByVisitDateDesc(User visitor);
    List<Visit> findByCondo_CondoId(UUID condoId);
    List<Visit> findByCondo_CondoIdOrderByVisitDateDesc(UUID condoId);
    List<Visit> findByCondo_CondoIdAndStatusIgnoreCaseOrderByVisitDateDesc(UUID condoId, String status);
    Optional<Visit> findByReferenceNumber(String referenceNumber);
}
