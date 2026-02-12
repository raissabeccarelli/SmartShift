package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;

@Service
public class TurnoService {

    @Autowired private DipendenteRepository dipendenteRepository;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private AssenzaRepository assenzaRepository;

    // Classe interna per definire le fasce orarie
    private static class ConfigurazioneSlot {
        LocalTime inizio;
        LocalTime fine;
        int minDipendenti;

        public ConfigurazioneSlot(int hInizio, int hFine, int minDipendenti) {
            this.inizio = LocalTime.of(hInizio, 0);
            this.fine = (hFine == 24) ? LocalTime.of(0, 0) : LocalTime.of(hFine, 0);
            this.minDipendenti = minDipendenti;
        }
    }

    // --- CONFIGURAZIONE DINAMICA ---
    // Non è più "static final", ma una lista modificabile che ricorda l'ultima generazione fatta.
    // Parte con dei valori di default per quando avvii l'app la prima volta.
    private List<ConfigurazioneSlot> currentConfig = new ArrayList<>(List.of(
        new ConfigurazioneSlot(6, 12, 3), 
        new ConfigurazioneSlot(12, 18, 4), 
        new ConfigurazioneSlot(18, 23, 5) 
    ));

    // Questo metodo viene chiamato dal Frontend per disegnare i box tratteggiati
    public List<Map<String, Object>> getConfigurazioneSlot() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        // Leggiamo dalla configurazione CORRENTE (aggiornata), non da quella di default fissa
        for (ConfigurazioneSlot slot : currentConfig) {
            result.add(Map.of(
                "inizio", slot.inizio.format(formatter),
                "fine", slot.fine.format(formatter),
                "min", slot.minDipendenti
            ));
        }
        return result;
    }

    // --- GESTIONE ASSENZE ---
    public void aggiungiAssenza(Long dipendenteId, LocalDate dataInizio, LocalDate dataFine, String tipo, String motivazione) {
        Dipendente d = dipendenteRepository.findById(dipendenteId)
                .orElseThrow(() -> new RuntimeException("Dipendente non trovato!"));

        if (dataFine == null) dataFine = dataInizio;
        long giorniRichiesti = java.time.temporal.ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;

        if (tipo.equalsIgnoreCase("FERIE")) {
            if (d.getFerieResidue() < giorniRichiesti) {
                throw new RuntimeException("ERRORE: Ferie insufficienti! Hai " + d.getFerieResidue() + " giorni.");
            }
            d.setFerieResidue(d.getFerieResidue() - (int) giorniRichiesti);
            dipendenteRepository.save(d);
        }

        if (tipo.equalsIgnoreCase("PERMESSO") && (motivazione == null || motivazione.trim().isEmpty())) {
            throw new RuntimeException("ERRORE: Motivazione obbligatoria per i permessi!");
        }

        LocalDate current = dataInizio;
        while (!current.isAfter(dataFine)) {
            if(assenzaRepository.findByDipendenteAndData(d, current).isEmpty()) {
                assenzaRepository.save(new Assenza(current, tipo, d, motivazione));
            }
            current = current.plusDays(1);
        }
    }

    // --- GENERATORE TURNI DINAMICO ---
    @Transactional
    public void generaTurniPerSettimana(LocalDate dataInizio, int minMattina, int minPomeriggio, int minSera) {
        LocalDate dataFine = dataInizio.plusDays(6);

        // 1. PULIZIA FORZATA
        turnoRepository.deleteTurniInRange(dataInizio, dataFine);
        turnoRepository.flush(); // Fondamentale per evitare letture "sporche" dalla cache

        List<Dipendente> dipendenti = dipendenteRepository.findAll();

        // 2. Creiamo la nuova configurazione richiesta dall'utente
        List<ConfigurazioneSlot> slotsDinamici = List.of(
            new ConfigurazioneSlot(6, 12, minMattina),
            new ConfigurazioneSlot(12, 18, minPomeriggio),
            new ConfigurazioneSlot(18, 23, minSera)
        );

        // --- PUNTO CHIAVE: AGGIORNIAMO LA MEMORIA DEL SISTEMA ---
        // Salviamo questa configurazione in 'currentConfig' così il frontend,
        // al refresh, saprà che i nuovi minimi sono questi e non darà errore rosso.
        this.currentConfig = new ArrayList<>(slotsDinamici);

        // 3. Generazione vera e propria
        for (LocalDate data = dataInizio; !data.isAfter(dataFine); data = data.plusDays(1)) {
            for (ConfigurazioneSlot slot : slotsDinamici) {
                Collections.shuffle(dipendenti); // Randomizza per equità
                assegnaTurnoMigliore(data, slot.inizio, slot.fine, slot.minDipendenti, dipendenti);
            }
        }
    }

    private void assegnaTurnoMigliore(LocalDate data, LocalTime inizio, LocalTime fine, int numeroMinimo, List<Dipendente> dipendenti) {
        LocalDate inizioSettimana = data.with(java.time.DayOfWeek.MONDAY);
        LocalDate fineSettimana = data.with(java.time.DayOfWeek.SUNDAY);

        // Calcolo durata slot
        int durataSlot = fine.getHour() - inizio.getHour();
        if (durataSlot < 0) durataSlot += 24; 
        if (durataSlot == 0 && fine.equals(LocalTime.of(0,0))) durataSlot = 24 - inizio.getHour();

        int personeAssegnate = 0;
        LocalDateTime startNuovoTurno = LocalDateTime.of(data, inizio);

        for (Dipendente d : dipendenti) {
            if (personeAssegnate >= numeroMinimo) break; 

            // 1. Controllo Assenze
            if (!assenzaRepository.findByDipendenteAndData(d, data).isEmpty()) continue;
            
            // 2. Controllo Riposo 11 ore
            List<Turno> turniRecenti = turnoRepository.findByDipendenteAndDataBetweenOrderByDataDescOraFineDesc(d, data.minusDays(1), data);
            if (!turniRecenti.isEmpty()) {
                Turno ultimo = turniRecenti.get(0);
                LocalDateTime fineUltimo = LocalDateTime.of(ultimo.getData(), ultimo.getOraFine());
                if (ultimo.getOraFine().isBefore(ultimo.getOraInizio())) fineUltimo = fineUltimo.plusDays(1);
                
                if (java.time.temporal.ChronoUnit.HOURS.between(fineUltimo, startNuovoTurno) < 11) continue;
            }

            // 3. Controllo Sovrapposizioni
            if (!turnoRepository.findByDipendenteAndData(d, data).isEmpty()) continue;

            // 4. Controllo Monte Ore Settimanale
            List<Turno> turniSettimana = turnoRepository.findByDipendenteAndDataBetween(d, inizioSettimana, fineSettimana);
            int oreGiaLavorate = 0;
            for (Turno t : turniSettimana) {
                int h = t.getOraFine().getHour() - t.getOraInizio().getHour();
                if (h < 0) h += 24;
                oreGiaLavorate += h;
            }

            int oreDaAssegnare = Math.min(durataSlot, d.getOreGiornaliereMax());
            oreDaAssegnare = Math.min(oreDaAssegnare, d.getOreSettimanaliContratto() - oreGiaLavorate);

            if (oreDaAssegnare <= 0) continue; 
            
            // Creazione e Salvataggio
            LocalTime nuovaFine = inizio.plusHours(oreDaAssegnare);
            Turno nuovoTurno = new Turno(data, inizio, nuovaFine, d);
            turnoRepository.save(nuovoTurno);
            personeAssegnate++;
        }
    }
}