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

    // Service contenente la logica per la generazione dei turni
    @Autowired private TurnoService turnoService;
    // Repository per l'accesso ai dati
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private DipendenteRepository dipendenteRepository;
    @Autowired private AssenzaRepository assenzaRepository;

    // Configurazione degli slot orari
    @GetMapping("/config-slot")
    public ResponseEntity<?> getConfigurazioneSlot() {
        return ResponseEntity.ok(turnoService.getConfigurazioneSlot());
    }

    // RICHIESTA DI GENERAZIONE DEI TURNI SETTIMANALI
    public static class GenerazioneRequest {
        private String data;
        private int minMattina;
        private int minPomeriggio;
        private int minSera;
        private int minNotte;

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }

        public int getMinMattina() { return minMattina; }
        public void setMinMattina(int minMattina) { this.minMattina = minMattina; }

        public int getMinPomeriggio() { return minPomeriggio; }
        public void setMinPomeriggio(int minPomeriggio) { this.minPomeriggio = minPomeriggio; }

        public int getMinSera() { return minSera; }
        public void setMinSera(int minSera) { this.minSera = minSera; }

        public int getMinNotte() { return minNotte; }
        public void setMinNotte(int minNotte) { this.minNotte = minNotte; }
    }

    // Genera i turni a partire dal lunedì della settimana selezionata
    @PostMapping("/genera")
    public ResponseEntity<?> generaTurni(@RequestBody GenerazioneRequest req) {
        try {
            if (req.getData() == null) return ResponseEntity.badRequest().body(Map.of("error", "Data mancante"));
            LocalDate start = LocalDate.parse(req.getData());
            LocalDate lunedi = start.with(java.time.DayOfWeek.MONDAY);

            turnoService.generaTurniPerSettimana(
                lunedi,
                req.getMinMattina(),
                req.getMinPomeriggio(),
                req.getMinSera(),
                req.getMinNotte()
            );

            return ResponseEntity.ok(Map.of("message", "Turni generati per la settimana del " + lunedi));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // LETTURA DEI TURNI
    @GetMapping
    public List<Turno> getTuttiTurni() {
        return turnoRepository.findAll();
    }

    // INSERIMENTO MANUALE
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

    // Elimina il turno tramite id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminaTurno(@PathVariable Long id) {
        if (turnoRepository.existsById(id)) {
            turnoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Turno eliminato"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Turno non trovato"));
        }
    }

    // GESTIONE ASSENZE
    public static class AssenzaRequest {
        public Long dipendenteId;
        public String dataInizio;
        public String dataFine;
        public String tipo;
        public String motivazione;
    }

    // Inserisce un'assenza su un intervallo di date
    // Se il tipo è FERIE, scala i giorni inseriti dalle ferie residue del dipendente
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

            int giorniInseriti = 0;
            LocalDate current = start;

            while (!current.isAfter(end)) {
                // Evita duplicati
                if (assenzaRepository.findByDipendenteAndData(dip, current).isEmpty()) {
                    Assenza a = new Assenza();
                    a.setDipendente(dip);
                    a.setData(current);
                    a.setTipo(req.tipo);
                    a.setMotivazione(req.motivazione);
                    assenzaRepository.save(a);
                    giorniInseriti++;
                }
                current = current.plusDays(1);
            }

            // Scala le ferie residue solo per assenze di tipo FERIE
            if ("FERIE".equals(req.tipo) && giorniInseriti > 0) {
                dip.setFerieResidue(dip.getFerieResidue() - giorniInseriti);
                dipendenteRepository.save(dip);
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

    // Elimina un'assenza tramite id
    // Se il tipo era FERIE, reintegra il giorno nelle ferie residue del dipendente
    @DeleteMapping("/assenza/{id}")
    public ResponseEntity<?> eliminaAssenza(@PathVariable Long id) {
        return assenzaRepository.findById(id).map(assenza -> {
            if ("FERIE".equals(assenza.getTipo())) {
                Dipendente dip = assenza.getDipendente();
                dip.setFerieResidue(dip.getFerieResidue() + 1);
                dipendenteRepository.save(dip);
            }
            assenzaRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Assenza eliminata con successo"));
        }).orElse(ResponseEntity.status(404).body(Map.of("error", "Assenza non trovata")));
    }

    // GESTIONE DIPENDENTI
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