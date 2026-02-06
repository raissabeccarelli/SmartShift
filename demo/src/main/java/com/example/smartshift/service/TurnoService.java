package com.example.smartshift.service;

import com.example.smartshift.model.*;
import com.example.smartshift.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Collections;

// Logica del sistema
@Service
public class TurnoService {
    @Autowired 
    private AssenzaRepository assenzaRepository;
import java.time.LocalTime;
import java.util.List;

@Service
public class TurnoService {

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Autowired
    private TurnoRepository turnoRepository;

    // Serve per determinare il minimo di persone che deve esserci in turno
    private static class ConfigurazioneSlot {
        LocalTime inizio;
        LocalTime fine;
        int minDipendenti; // <--- Qui decidiamo quanti ne servono!

        public ConfigurazioneSlot(int hInizio, int hFine, int minDipendenti) {
            this.inizio = LocalTime.of(hInizio, 0);
            this.fine = LocalTime.of(hFine, 0);
            this.minDipendenti = minDipendenti;
        }
    }

    // Serve per definire gli slot lavorativi giornalieri e il numero minimo di persone
    public void generaTurniPerSettimana(LocalDate dataInizio) {
        List<Dipendente> dipendenti = dipendenteRepository.findAll();
        LocalDate dataFine = dataInizio.plusDays(7);

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

    private void assegnaTurnoMigliore(LocalDate data, LocalTime inizio, LocalTime fine, int numeroMinimo, List<Dipendente> dipendenti) {
        
        LocalDate inizioSettimana = data.with(java.time.DayOfWeek.MONDAY);
        LocalDate fineSettimana = data.with(java.time.DayOfWeek.SUNDAY);

        // Calcoliamo durata slot
        int durataSlot = fine.getHour() - inizio.getHour();
        if (durataSlot < 0) durataSlot += 24; 

        int personeAssegnate=0;

        // Creiamo il timestamp dell'INIZIO del nuovo turno proposto
        LocalDateTime startNuovoTurno = LocalDateTime.of(data, inizio);

        for (Dipendente d : dipendenti) {
            if (personeAssegnate >= numeroMinimo) break;
            if (!assenzaRepository.findByDipendenteAndData(d, data).isEmpty()) {
                System.out.println(d.getCognome() + " è assente (Ferie/Malattia). Salto.");
                continue;
            }
            // --- 1. CONTROLLO RIPOSO 11 ORE (NOVITÀ) ---
            // Cerchiamo turni di Ieri e Oggi
            List<Turno> turniRecenti = turnoRepository.findByDipendenteAndDataBetweenOrderByDataDescOraFineDesc(
                d, data.minusDays(1), data
            );

            if (!turniRecenti.isEmpty()) {
                Turno ultimoTurno = turniRecenti.get(0);
                
                // Calcoliamo quando è finito l'ultimo turno
                LocalDateTime fineUltimoTurno = LocalDateTime.of(ultimoTurno.getData(), ultimoTurno.getOraFine());
                
                // Se il turno finiva il giorno dopo (es. notte), aggiungiamo 1 giorno
                if (ultimoTurno.getOraFine().isBefore(ultimoTurno.getOraInizio())) {
                    fineUltimoTurno = fineUltimoTurno.plusDays(1);
                }

                // CALCOLO ORE DI STACCO
                long oreDiRiposo = java.time.temporal.ChronoUnit.HOURS.between(fineUltimoTurno, startNuovoTurno);

                if (oreDiRiposo < 11) {
                    System.out.println( d.getCognome() + " deve riposare! (Stacco: " + oreDiRiposo + "h). Salto.");
                    continue; 
                }
            }


            // 2. Controllo: Ha già lavorato OGGI? (Ridondante col check sopra, ma utile per sicurezza)
            if (!turnoRepository.findByDipendenteAndData(d, data).isEmpty()) continue;

            // 3. Controllo Monte Ore Settimanale
            List<Turno> turniSettimana = turnoRepository.findByDipendenteAndDataBetween(d, inizioSettimana, fineSettimana);
            int oreGiaLavorate = 0;
            for (Turno t : turniSettimana) {
                int h = t.getOraFine().getHour() - t.getOraInizio().getHour();
                if (h < 0) h += 24;
                oreGiaLavorate += h;
            }

            // 4. Logica Orario Variabile (Part-time)
            int maxGiornaliero = d.getOreGiornaliereMax();
            int oreDaAssegnare = Math.min(durataSlot, maxGiornaliero);
            
            int oreRimanentiSettimana = d.getOreSettimanaliContratto() - oreGiaLavorate;
            oreDaAssegnare = Math.min(oreDaAssegnare, oreRimanentiSettimana);

            if (oreDaAssegnare <= 0) continue;

            LocalTime nuovaFine = inizio.plusHours(oreDaAssegnare);

            // Assegnazione
            Turno nuovoTurno = new Turno(data, inizio, nuovaFine, d);
            turnoRepository.save(nuovoTurno);
            
            System.out.println("Assegnato " + inizio + "-" + nuovaFine + " a " + d.getCognome() + 
                               " [" + oreDaAssegnare + "h] (Riposo rispettato)");
            
            personeAssegnate++;
        }

        if (personeAssegnate < numeroMinimo) {
            int mancanti = numeroMinimo - personeAssegnate;
            System.err.println("ALLARME: Fascia " + data + " " + inizio + "-" + fine + 
                               " non ha abbastanza dipendenti assegnati: " + personeAssegnate + "/" + numeroMinimo + ". Mancano ancora " + mancanti + " persone.");
        }

    }
}
    public void generaTurniPerSettimana(LocalDate inizioSettimana) {
        List<Dipendente> dipendenti = dipendenteRepository.findAll();

        // Ciclo per i 7 giorni della settimana
        for (int i = 0; i < 7; i++) {
            LocalDate giornoCorrente = inizioSettimana.plusDays(i);
            System.out.println("Elaborazione giorno: " + giornoCorrente);

            // Definiamo 3 slot orari fissi (Mattina, Pom, Sera)
            LocalTime[][] slotOrari = {
                {LocalTime.of(6, 0), LocalTime.of(14, 0)},
                {LocalTime.of(14, 0), LocalTime.of(22, 0)},
                {LocalTime.of(22, 0), LocalTime.of(6, 0)}
            };

            for (LocalTime[] slot : slotOrari) {
                assegnaTurnoMigliore(giornoCorrente, slot[0], slot[1], dipendenti);
            }
        }
    }

    private void assegnaTurnoMigliore(LocalDate data, LocalTime inizio, LocalTime fine, List<Dipendente> dipendenti) {
        for (Dipendente d : dipendenti) {
            
            // 1. Controllo base: Ha già lavorato oggi?
            List<Turno> turniOggi = turnoRepository.findByDipendenteAndData(d, data);
            if (!turniOggi.isEmpty()) {
                continue; // Passa al prossimo dipendente
            }

            // SE ASSEGNIAMO IL TURNO:
            // Usiamo l'ordine corretto: Data, Inizio, Fine, Dipendente
            Turno nuovoTurno = new Turno(data, inizio, fine, d);
            
            turnoRepository.save(nuovoTurno);
            System.out.println("Assegnato turno " + inizio + "-" + fine + " a " + d.getCognome());
            
            break; // Abbiamo coperto questo slot, stop ciclo dipendenti
        }
    }
}
