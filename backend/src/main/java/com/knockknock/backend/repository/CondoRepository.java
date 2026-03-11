package com.knockknock.backend.repository;

import com.knockknock.backend.entity.Condo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CondoRepository extends JpaRepository<Condo, UUID> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
}