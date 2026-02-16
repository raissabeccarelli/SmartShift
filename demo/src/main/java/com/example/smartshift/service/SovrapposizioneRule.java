package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.TurnoRepository;

@Component
@Order(2)
public class SovrapposizioneRule implements ShiftRule {

    @Autowired
    private TurnoRepository turnoRepository;

    @Override
    public boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine) {
        // Recuperiamo tutti i turni di OGGI per questo dipendente
        List<Turno> turniOggi = turnoRepository.findByDipendenteAndData(dipendente, data);

        // Se non ha turni, è valido
        if (turniOggi.isEmpty()) return true;

        // Se ha turni, controlliamo che NON si sovrappongano con quello nuovo
        for (Turno t : turniOggi) {
            // Logica di sovrapposizione: (InizioA < FineB) E (FineA > InizioB)
            // Usiamo ! (NOT) perché il metodo deve ritornare TRUE se è valido (cioè se NON si toccano)
            
            boolean sovrapposti = isOverlapping(inizio, fine, t.getOraInizio(), t.getOraFine());
            if (sovrapposti) {
                return false; // C'è collisione, regola fallita
            }
        }
        return true; // Nessuna collisione trovata
    }

    private boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Gestione normalizzata dei minuti per evitare problemi con la mezzanotte
        int s1 = start1.getHour() * 60 + start1.getMinute();
        int e1 = end1.getHour() * 60 + end1.getMinute();
        int s2 = start2.getHour() * 60 + start2.getMinute();
        int e2 = end2.getHour() * 60 + end2.getMinute();

        // Se l'orario di fine è 0 (mezzanotte), lo consideriamo come 24:00 (1440 minuti)
        if (e1 <= s1) e1 += 1440; 
        if (e2 <= s2) e2 += 1440;

        return s1 < e2 && e1 > s2;
    }
}