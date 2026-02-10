package com.example.smartshift.service;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class TurnoService {

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    @Autowired
    private AssenzaRepository assenzaRepository;

    // --- CLASSE DI CONFIGURAZIONE PER GLI SLOT ---
    private static class ConfigurazioneSlot {
        LocalTime inizio;
        LocalTime fine;
        int minDipendenti;

        public ConfigurazioneSlot(int hInizio, int hFine, int minDipendenti) {
            this.inizio = LocalTime.of(hInizio, 0);
            this.fine = LocalTime.of(hFine, 0);
            this.minDipendenti = minDipendenti;
        }
    }

    // Aggiungo assenze
    public void aggiungiAssenza(Long dipendenteId, LocalDate dataInizio, LocalDate dataFine, String tipo, String motivazione) {
        Dipendente d = dipendenteRepository.findById(dipendenteId)
                .orElseThrow(() -> new RuntimeException("Dipendente non trovato!"));

        if (dataFine == null) dataFine = dataInizio;
        long giorniRichiesti = java.time.temporal.ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;
        // Controllo le ferie residue
        if (tipo.equalsIgnoreCase("FERIE")) {
            if (d.getFerieResidue() < giorniRichiesti) {
                throw new RuntimeException("ERRORE: Ferie insufficienti! Hai " + d.getFerieResidue() + " giorni, ma ne chiedi " + giorniRichiesti);
            }
            // Scaliamo le ferie da quelle residue
            d.setFerieResidue(d.getFerieResidue() - (int) giorniRichiesti);
            dipendenteRepository.save(d);
        }

        // Per i permessi aggiungo la motivazione
        if (tipo.equalsIgnoreCase("PERMESSO") && (motivazione == null || motivazione.trim().isEmpty())) {
            throw new RuntimeException("ERRORE: Per i permessi è obbligatoria la motivazione!");
        }

        // Salva una riga per ogni giorno nel database
        LocalDate current = dataInizio;
        while (!current.isAfter(dataFine)) {
            assenzaRepository.save(new Assenza(current, tipo, d, motivazione));
            System.out.println("Assenza registrata: " + d.getCognome() + " il " + current);
            current = current.plusDays(1);
        }
    }

    // Algoritmo di generatore turni
    public void generaTurniPerSettimana(LocalDate dataInizio) {
        List<Dipendente> dipendenti = dipendenteRepository.findAll();
        LocalDate dataFine = dataInizio.plusDays(7);

        // Configurazione
        List<ConfigurazioneSlot> slots = List.of(
            new ConfigurazioneSlot(6, 12, 3),
            new ConfigurazioneSlot(12, 18, 4),
            new ConfigurazioneSlot(18, 00, 5),
            new ConfigurazioneSlot(00, 6, 2)
        );

        for (LocalDate data = dataInizio; data.isBefore(dataFine); data = data.plusDays(1)) {
            System.out.println("Elaborazione giorno: " + data );

            for (ConfigurazioneSlot slot : slots) {
                Collections.shuffle(dipendenti);
                assegnaTurnoMigliore(data, slot.inizio, slot.fine, slot.minDipendenti, dipendenti);
            }
        }
    }

    // Algoritmo di assegnazione dei turni
    private void assegnaTurnoMigliore(LocalDate data, LocalTime inizio, LocalTime fine, int numeroMinimo, List<Dipendente> dipendenti) {
        
        LocalDate inizioSettimana = data.with(java.time.DayOfWeek.MONDAY);
        LocalDate fineSettimana = data.with(java.time.DayOfWeek.SUNDAY);

        int durataSlot = fine.getHour() - inizio.getHour();
        if (durataSlot < 0) durataSlot += 24; 

        int personeAssegnate = 0;
        LocalDateTime startNuovoTurno = LocalDateTime.of(data, inizio);

        for (Dipendente d : dipendenti) {
            
            // 1. STOP se abbiamo raggiunto il numero richiesto
            if (personeAssegnate >= numeroMinimo) break; 

            // --- CONTROLLI ---
            
            // A. Assenza
            if (!assenzaRepository.findByDipendenteAndData(d, data).isEmpty()) {
                continue;
            }

            // B. Riposo 11 ore
            List<Turno> turniRecenti = turnoRepository.findByDipendenteAndDataBetweenOrderByDataDescOraFineDesc(
                d, data.minusDays(1), data
            );
            if (!turniRecenti.isEmpty()) {
                Turno ultimoTurno = turniRecenti.get(0);
                LocalDateTime fineUltimoTurno = LocalDateTime.of(ultimoTurno.getData(), ultimoTurno.getOraFine());
                if (ultimoTurno.getOraFine().isBefore(ultimoTurno.getOraInizio())) {
                    fineUltimoTurno = fineUltimoTurno.plusDays(1);
                }
                long oreDiRiposo = java.time.temporal.ChronoUnit.HOURS.between(fineUltimoTurno, startNuovoTurno);

                if (oreDiRiposo < 11) {
                    continue; 
                }
            }

            // C. Già lavorato oggi?
            if (!turnoRepository.findByDipendenteAndData(d, data).isEmpty()) continue;

            // D. Monte ore settimanale
            List<Turno> turniSettimana = turnoRepository.findByDipendenteAndDataBetween(d, inizioSettimana, fineSettimana);
            int oreGiaLavorate = 0;
            for (Turno t : turniSettimana) {
                int h = t.getOraFine().getHour() - t.getOraInizio().getHour();
                if (h < 0) h += 24;
                oreGiaLavorate += h;
            }

            // E. Part-time check
            int maxGiornaliero = d.getOreGiornaliereMax();
            int oreDaAssegnare = Math.min(durataSlot, maxGiornaliero);
            int oreRimanentiSettimana = d.getOreSettimanaliContratto() - oreGiaLavorate;
            oreDaAssegnare = Math.min(oreDaAssegnare, oreRimanentiSettimana);

            if (oreDaAssegnare <= 0) continue;

            // --- ASSEGNAZIONE ---
            LocalTime nuovaFine = inizio.plusHours(oreDaAssegnare);
            Turno nuovoTurno = new Turno(data, inizio, nuovaFine, d);
            turnoRepository.save(nuovoTurno);
            
            System.out.println("   ✅ (" + (personeAssegnate+1) + "/" + numeroMinimo + ") " + 
                               d.getCognome() + " copre " + inizio + "-" + nuovaFine);
            
            personeAssegnate++;
        }

        // Allarme finale
        if (personeAssegnate < numeroMinimo) {
            int mancanti = numeroMinimo - personeAssegnate;
            System.err.println("ALLARME: Fascia " + data + " " + inizio + "-" + fine + 
                               " non ha abbastanza personale: " + personeAssegnate + "/" + numeroMinimo + ". Mancano ancora " + mancanti + " persone.");
        }
    }
}