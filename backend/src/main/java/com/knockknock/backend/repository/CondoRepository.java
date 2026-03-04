package com.knockknock.backend.repository;

import com.knockknock.backend.entity.Condo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CondoRepository extends JpaRepository<Condo, Long> {
    boolean existsByName(String name);
}