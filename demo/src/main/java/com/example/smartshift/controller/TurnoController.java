package com.example.smartshift.controller;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;
import com.example.smartshift.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turni")
public class TurnoController {

    @Autowired private TurnoService turnoService;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private DipendenteRepository dipendenteRepository;

    // 1. GENERAZIONE
    @PostMapping("/genera")
    public String generaTurni(@RequestParam(required = false) String data) {
        LocalDate start = (data != null) ? LocalDate.parse(data) : LocalDate.now();
        // Spostiamoci al lunedì di quella settimana
        LocalDate lunedi = start.with(java.time.DayOfWeek.MONDAY);
        
        turnoService.generaTurniPerSettimana(lunedi);
        return "Turni generati con successo per la settimana del " + lunedi;
    }

    // 2. LETTURA TURNI
    @GetMapping
    public List<Turno> getTuttiTurni() {
        return turnoRepository.findAll();
    }

    // 3. INSERIMENTO ASSENZA
    @PostMapping("/assenza")
    public String inserisciAssenza(@RequestParam Long dipendenteId, @RequestParam String data, @RequestParam String tipo) {
        turnoService.aggiungiAssenza(dipendenteId, LocalDate.parse(data), tipo);
        return "Assenza inserita!";
    }

    // 4. LISTA DIPENDENTI (Serve per il menu a tendina nel frontend)
    @GetMapping("/dipendenti")
    public List<Dipendente> getDipendenti() {
        return dipendenteRepository.findAll();
    }

    // AGGIUNGE UN NUOVO DIPENDETE
    @PostMapping("/dipendenti")
    public Dipendente aggiungiDipendente(@RequestBody Dipendente nuovoDipendente) {
        return dipendenteRepository.save(nuovoDipendente);
    }
}