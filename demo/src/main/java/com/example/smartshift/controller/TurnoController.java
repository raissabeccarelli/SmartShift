package com.example.smartshift.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    // --- CONFIGURAZIONE SLOT ---
    @GetMapping("/config-slot")
    public ResponseEntity<?> getConfigurazioneSlot() {
        return ResponseEntity.ok(turnoService.getConfigurazioneSlot());
    }

    // --- 1. GENERAZIONE ---
    public static class GenerazioneRequest {
        private String data;
        private int minMattina;
        private int minPomeriggio;
        private int minSera;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public int getMinMattina() { return minMattina; }
        public void setMinMattina(int minMattina) { this.minMattina = minMattina; }
        public int getMinPomeriggio() { return minPomeriggio; }
        public void setMinPomeriggio(int minPomeriggio) { this.minPomeriggio = minPomeriggio; }
        public int getMinSera() { return minSera; }
        public void setMinSera(int minSera) { this.minSera = minSera; }
    }

    @PostMapping("/genera")
    public ResponseEntity<?> generaTurni(@RequestBody GenerazioneRequest req) {
        try {
            if (req.getData() == null) return ResponseEntity.badRequest().body(Map.of("error", "Data mancante"));
            LocalDate start = LocalDate.parse(req.getData());
            LocalDate lunedi = start.with(java.time.DayOfWeek.MONDAY);
            
            turnoService.generaTurniPerSettimana(lunedi, req.getMinMattina(), req.getMinPomeriggio(), req.getMinSera());
            return ResponseEntity.ok(Map.of("message", "Turni generati per la settimana del " + lunedi));
        } catch (Exception e) {
            e.printStackTrace(); 
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

    // --- 4. GESTIONE ASSENZE (AGGIORNATO PER RANGE DI DATE) ---
    public static class AssenzaRequest {
        public Long dipendenteId;
        public String dataInizio; // Modificato: Da singola data a Inizio
        public String dataFine;   // Aggiunto: Fine
        public String tipo;
        public String motivazione;
    }

    @PostMapping("/assenza")
    public ResponseEntity<?> inserisciAssenza(@RequestBody AssenzaRequest req) {
        try {
            LocalDate start = LocalDate.parse(req.dataInizio);
            LocalDate end = LocalDate.parse(req.dataFine);

            if (end.isBefore(start)) {
                return ResponseEntity.badRequest().body(Map.of("error", "La data di fine non può essere precedente all'inizio"));
            }

            Dipendente dip = dipendenteRepository.findById(req.dipendenteId)
                    .orElseThrow(() -> new RuntimeException("Dipendente non trovato"));

            // CICLO: Inseriamo una riga nel DB per ogni giorno compreso tra start ed end
            LocalDate current = start;
            while (!current.isAfter(end)) {
                Assenza a = new Assenza();
                a.setDipendente(dip);
                a.setData(current); // Data del singolo giorno
                a.setTipo(req.tipo);
                // Usiamo setMotivazione come richiesto
                a.setMotivazione(req.motivazione); 

                assenzaRepository.save(a);
                
                // Avanziamo di un giorno
                current = current.plusDays(1);
            }

            return ResponseEntity.ok(Map.of("message", "Assenze inserite dal " + start + " al " + end));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/assenze")
    public List<Assenza> getTutteAssenze() {
        return assenzaRepository.findAll();
    }

    // Metodo per eliminare le assenze (che mancava prima)
    @DeleteMapping("/assenza/{id}")
    public ResponseEntity<?> eliminaAssenza(@PathVariable Long id) {
        if (assenzaRepository.existsById(id)) {
            assenzaRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Assenza eliminata con successo"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", "Assenza non trovata"));
        }
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

    @Transactional
    @DeleteMapping("/dipendenti/{id}")
    public ResponseEntity<?> eliminaDipendente(@PathVariable Long id) {
        try {
            Dipendente d = dipendenteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Dipendente non trovato"));
            turnoRepository.deleteByDipendente(d);
            dipendenteRepository.delete(d);
            return ResponseEntity.ok().body(Map.of("message", "Eliminato con successo"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}