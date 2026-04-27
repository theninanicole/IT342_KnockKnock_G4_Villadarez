package edu.cit.villadarez.knockknock.service;

import edu.cit.villadarez.knockknock.entity.Condo;
import java.util.List;

public interface CondoService {
    List<Condo> getAllCondos();
    Condo getCondoById(java.util.UUID condoId);
}
