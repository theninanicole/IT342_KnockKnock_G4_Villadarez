package com.knockknock.backend.repository;

import com.knockknock.backend.entity.VisitFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VisitFileRepository extends JpaRepository<VisitFile, UUID> {
    List<VisitFile> findByVisit_VisitId(UUID visitId);

    @Query("SELECT new com.knockknock.backend.dto.VisitFileDTO(f.fileId, f.fileName, f.fileUrl, f.filePath) FROM VisitFile f WHERE f.visit.visitId = :visitId")
    List<com.knockknock.backend.dto.VisitFileDTO> findFileNamesByVisitId(@Param("visitId") UUID visitId);
}
