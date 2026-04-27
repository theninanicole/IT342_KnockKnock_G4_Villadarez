package edu.cit.villadarez.knockknock.controller;

import edu.cit.villadarez.knockknock.entity.Condo;
import edu.cit.villadarez.knockknock.service.CondoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/condos")
@CrossOrigin(origins = "http://localhost:5173")
public class CondoController {

    private final CondoService condoService;

    public CondoController(CondoService condoService) {
        this.condoService = condoService;
    }

    @GetMapping
    public ResponseEntity<List<Condo>> getAllCondos() {
        List<Condo> condos = condoService.getAllCondos();
        return ResponseEntity.ok(condos);
    }

    @GetMapping("/{condoId}")
    public ResponseEntity<Condo> getCondoById(@PathVariable String condoId) {
        Condo condo = condoService.getCondoById(java.util.UUID.fromString(condoId));
        return ResponseEntity.ok(condo);
    }
}
