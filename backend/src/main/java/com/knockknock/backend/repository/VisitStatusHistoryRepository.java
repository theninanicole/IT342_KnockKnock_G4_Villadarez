package com.knockknock.backend.repository;

import com.knockknock.backend.entity.VisitStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VisitStatusHistoryRepository extends JpaRepository<VisitStatusHistory, UUID> {
    List<VisitStatusHistory> findByVisit_Condo_CondoIdOrderByChangedAtDesc(UUID condoId);
}
