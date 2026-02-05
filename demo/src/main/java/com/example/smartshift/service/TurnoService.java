package com.example.smartshift.service;

import com.example.smartshift.model.*;
import com.example.smartshift.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TurnoService {

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Autowired
    private TurnoRepository turnoRepository;

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