package com.knockknock.backend.service;

import com.knockknock.backend.entity.Condo;
import com.knockknock.backend.repository.CondoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
import java.util.UUID;

@Service
public class CondoServiceImpl implements CondoService {

    private final CondoRepository condoRepository;

    public CondoServiceImpl(CondoRepository condoRepository) {
        this.condoRepository = condoRepository;
    }

    @Override
    public List<Condo> getAllCondos() {
        return condoRepository.findAll();
    }

    @Override
    public Condo getCondoById(UUID condoId) {
        return condoRepository.findById(condoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Condominium not found"));
    }
}
