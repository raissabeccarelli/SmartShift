package com.example.smartshift.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;
import com.example.smartshift.service.TurnoService;

@RestController
@RequestMapping("/api/turni")
public class TurnoController {

    @Autowired private TurnoService turnoService;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private DipendenteRepository dipendenteRepository;
    @Autowired private AssenzaRepository assenzaRepository;

    // --- NUOVO ENDPOINT: Configurazione Slot ---
    // Serve al frontend per calcolare i buchi di organico
    @GetMapping("/config-slot")
    public ResponseEntity<?> getConfigurazioneSlot() {
        return ResponseEntity.ok(turnoService.getConfigurazioneSlot());
    }

    // --- 1. GENERAZIONE ---
    @PostMapping("/genera")
    public ResponseEntity<?> generaTurni(@RequestParam(required = false) String data) {
        try {
            LocalDate start = (data != null) ? LocalDate.parse(data) : LocalDate.now();
            LocalDate lunedi = start.with(java.time.DayOfWeek.MONDAY);
            
            turnoService.generaTurniPerSettimana(lunedi);
            return ResponseEntity.ok(Map.of("message", "Turni generati per la settimana del " + lunedi));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // --- 2. LETTURA ---
    @GetMapping
    public List<Turno> getTuttiTurni() {
        return turnoRepository.findAll();
    }

    // --- 3. GESTIONE MANUALE ---
    public static class TurnoManualeRequest {
        public Long dipendenteId;
        public String data;
        public String oraInizio;
        public String oraFine;
    }

    @PostMapping("/manuale")
    public ResponseEntity<?> aggiungiTurnoManuale(@RequestBody TurnoManualeRequest req) {
        try {
            Dipendente d = dipendenteRepository.findById(req.dipendenteId)
                    .orElseThrow(() -> new RuntimeException("Dipendente non trovato"));

            Turno t = new Turno();
            t.setData(LocalDate.parse(req.data));
            t.setOraInizio(LocalTime.parse(req.oraInizio));
            t.setOraFine(LocalTime.parse(req.oraFine));
            t.setDipendente(d);

            turnoRepository.save(t);
            return ResponseEntity.ok(Map.of("message", "Turno aggiunto"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaTurno(@PathVariable Long id) {
        if (turnoRepository.existsById(id)) {
            turnoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Turno eliminato"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Turno non trovato"));
        }
    }

    // --- 4. GESTIONE ASSENZE ---
    public static class AssenzaRequest {
        public Long dipendenteId;
        public String data;
        public String tipo;
        public String motivazione;
    }

    @PostMapping("/assenza")
    public ResponseEntity<?> inserisciAssenza(@RequestBody AssenzaRequest req) {
        try {
            LocalDate start = LocalDate.parse(req.data);
            turnoService.aggiungiAssenza(req.dipendenteId, start, start, req.tipo, req.motivazione);
            return ResponseEntity.ok(Map.of("message", "Assenza inserita"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assenze")
    public List<Assenza> getTutteAssenze() {
        return assenzaRepository.findAll();
    }

    // --- 5. GESTIONE DIPENDENTI ---
    @GetMapping("/dipendenti")
    public List<Dipendente> getDipendenti() {
        return dipendenteRepository.findAll();
    }

    @PostMapping("/dipendenti")
    public Dipendente aggiungiDipendente(@RequestBody Dipendente nuovoDipendente) {
        return dipendenteRepository.save(nuovoDipendente);
    }
}