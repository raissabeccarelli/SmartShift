package com.example.smartshift.controller;

import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.TurnoRepository;
import com.example.smartshift.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turni")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TurnoRepository turnoRepository;

    // 1. Endpoint per GENERARE i turni
    // URL: GET /api/turni/genera
    @GetMapping("/genera")
    public String generaTurni() {
        // Calcoliamo il Lunedì di questa settimana
        LocalDate oggi = LocalDate.now();
        LocalDate lunediCorrente = oggi.with(java.time.DayOfWeek.MONDAY);

        // Generiamo i turni partendo da Lunedì (anche se è passato, li rigenera)
        turnoService.generaTurniPerSettimana(lunediCorrente); 
        
        return "Turni generati per la settimana del " + lunediCorrente;
    }

    // 2. Endpoint per LEGGERE i turni (JSON per il Frontend)
    // URL: GET /api/turni
    @GetMapping
    public List<Turno> getTuttiTurni() {
        return turnoRepository.findAll();
    }

    // 3. Endpoint per INSERIRE ASSENZE (Collegato al Service che abbiamo fatto prima)
    // URL: POST /api/turni/assenza?dipendenteId=1&data=2026-02-10&tipo=FERIE
    @PostMapping("/assenza")
    public String inserisciAssenza(
            @RequestParam Long dipendenteId,
            @RequestParam String data, 
            @RequestParam String tipo) {
        
        LocalDate dataAssenza = LocalDate.parse(data);
        turnoService.aggiungiAssenza(dipendenteId, dataAssenza, tipo);

        return "Assenza inserita con successo!";
    }
}