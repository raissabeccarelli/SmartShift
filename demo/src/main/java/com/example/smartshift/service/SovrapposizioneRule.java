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

/*
 * Regola che impedisce la sovrapposizione di turni
 * nello stesso giorno per uno stesso dipendente
 */
@Component
@Order(2) // Priorità: eseguita dopo il controllo delle assenza
public class SovrapposizioneRule implements ShiftRule {

    // Repository utilizzato per recuperare i turni già assegnati
    @Autowired
    private TurnoRepository turnoRepository;

    // Controllo di validità sulla non-sovrapposizione dei turni
    @Override
    public boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine) {
        // Recupera tutti i turni del giorno considerato per il dipendente
        List<Turno> turniOggi = turnoRepository.findByDipendenteAndData(dipendente, data);

        // Se non ha turni il giorno considerato, è valido
        if (turniOggi.isEmpty()) return true;

        // Se ha turni nel giorno considerato,
        // controlla che non si sovrappongano con quello nuovo
        for (Turno t : turniOggi) {
            
            boolean sovrapposti = isOverlapping(inizio, fine, t.getOraInizio(), t.getOraFine());
            if (sovrapposti) {
                return false; // C'è sovrapposizione
            }
        }
        return true; // Nessuna sovrapposizione trovata
    }

    /*
     * Verifica se avviene una sovrapposizione tra i turni
     * I turni A e B sono sovrapposti se (InizioA < FineB) AND (FineA > InizioB)
     * Ritorna TRUE se non si sovrappongono
    */
    private boolean isOverlapping(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Conversione in minuti, semplifica il confronto
        int s1 = start1.getHour() * 60 + start1.getMinute();
        int e1 = end1.getHour() * 60 + end1.getMinute();
        int s2 = start2.getHour() * 60 + start2.getMinute();
        int e2 = end2.getHour() * 60 + end2.getMinute();

        // Gestione dei turni che superano la mezzanotte 
        // (orario di fine minore o uguale all'orario di inizio, 
        // quindi termina il giorno successivo)
        if (e1 <= s1) e1 += 1440; 
        if (e2 <= s2) e2 += 1440;

        // Verifica tramite intersezione degli intervalli
        return s1 < e2 && e1 > s2;
    }
}