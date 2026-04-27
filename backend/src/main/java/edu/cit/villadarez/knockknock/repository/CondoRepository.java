package edu.cit.villadarez.knockknock.repository;

import edu.cit.villadarez.knockknock.entity.Condo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CondoRepository extends JpaRepository<Condo, UUID> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
}