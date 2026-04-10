package com.knockknock.backend.service;

import com.knockknock.backend.entity.Condo;
import java.util.List;

public interface CondoService {
    List<Condo> getAllCondos();
    Condo getCondoById(java.util.UUID condoId);
}
