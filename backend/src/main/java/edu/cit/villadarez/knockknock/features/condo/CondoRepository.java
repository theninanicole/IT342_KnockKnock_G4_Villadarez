package edu.cit.villadarez.knockknock.features.condo;

import edu.cit.villadarez.knockknock.features.condo.Condo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CondoRepository extends JpaRepository<Condo, UUID> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
}