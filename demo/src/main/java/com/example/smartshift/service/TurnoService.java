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

    // --- METODO PER AGGIUNGERE ASSENZE ---
    public void aggiungiAssenza(Long dipendenteId, LocalDate data, String tipo) {
        Dipendente d = dipendenteRepository.findById(dipendenteId)
                .orElseThrow(() -> new RuntimeException("Dipendente non trovato!"));
        assenzaRepository.save(new Assenza(data, tipo, d));
        System.out.println("Nuova assenza registrata per " + d.getCognome() + " il " + data);
    }

    // --- GENERATORE DI TURNI ---
    public void generaTurniPerSettimana(LocalDate dataInizio) {
        List<Dipendente> dipendenti = dipendenteRepository.findAll();
        LocalDate dataFine = dataInizio.plusDays(7);

        // Configurazione: Mattina (3 persone), Pomeriggio (2 persone), Notte (1 persona)
        List<ConfigurazioneSlot> slots = List.of(
            new ConfigurazioneSlot(6, 14, 3),
            new ConfigurazioneSlot(14, 22, 2),
            new ConfigurazioneSlot(22, 6, 1)
        );

        for (LocalDate data = dataInizio; data.isBefore(dataFine); data = data.plusDays(1)) {
            System.out.println("--- Elaborazione giorno: " + data + " ---");

            for (ConfigurazioneSlot slot : slots) {
                Collections.shuffle(dipendenti);
                assegnaTurnoMigliore(data, slot.inizio, slot.fine, slot.minDipendenti, dipendenti);
            }
        }
    }

    // --- ALGORITMO DI ASSEGNAZIONE ---
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