package edu.cit.villadarez.knockknock.repository;

import edu.cit.villadarez.knockknock.entity.VisitFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VisitFileRepository extends JpaRepository<VisitFile, UUID> {
    List<VisitFile> findByVisit_VisitId(UUID visitId);

    @Query("SELECT new edu.cit.villadarez.knockknock.dto.VisitFileDTO(f.fileId, f.fileName, f.fileUrl, f.filePath) FROM VisitFile f WHERE f.visit.visitId = :visitId")
    List<edu.cit.villadarez.knockknock.dto.VisitFileDTO> findFileNamesByVisitId(@Param("visitId") UUID visitId);
}
