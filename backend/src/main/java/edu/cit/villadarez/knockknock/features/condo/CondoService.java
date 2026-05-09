package edu.cit.villadarez.knockknock.features.condo;

import edu.cit.villadarez.knockknock.features.condo.Condo;
import java.util.List;

public interface CondoService {
    List<Condo> getAllCondos();
    Condo getCondoById(java.util.UUID condoId);
}
