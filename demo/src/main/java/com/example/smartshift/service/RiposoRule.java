package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.TurnoRepository;

/*
 * Regola che verifica il rispetto del riposo minimo di 11 ore
 * tra la fine di un turno e l'inizio del successivo
*/
@Component
@Order(3) // Priorità: eseguita dopo i controlli su assenze e sovrapposizione dei turni
public class RiposoRule implements ShiftRule {

    // Repository utilizzato per recuperare i turni precedenti del dipendente
    @Autowired
    private TurnoRepository turnoRepository;

    // Controllo di validità sul riposo minimo del dipendente
    @Override
    public boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine) {
        // recupera i turni nel giorno precedente
        List<Turno> turniIeri = turnoRepository.findByDipendenteAndData(dipendente, data.minusDays(1));
        
        // Se non esistono turni il giorno prima, il vincolo è automaticamente rispettato
        if (turniIeri.isEmpty()) return true;

        // Considera l'ultimo turno svolto il giorno precedente
        Turno ultimoTurnoIeri = turniIeri.get(turniIeri.size() - 1); 

        // Data ed ora dell'ultimo turno svolto il giorno precedente
        LocalDateTime fineIeri = LocalDateTime.of(ultimoTurnoIeri.getData(), ultimoTurnoIeri.getOraFine());
        
        // Gestione di un turno notturno che finisce dopo mezzanotte
        // L'orario di fine è precedente all'orario di inizio (giorno successivo)
        if (ultimoTurnoIeri.getOraFine().isBefore(ultimoTurnoIeri.getOraInizio())) {
            fineIeri = fineIeri.plusDays(1);
        }

        // Data ed ora di inizio del nuovo turno
        LocalDateTime inizioNuovo = LocalDateTime.of(data, inizio);
        // Calcolo delle ore di riposo tra la fine turno precedente e l'inizio del nuovo turno
        long oreDiStacco = ChronoUnit.HOURS.between(fineIeri, inizioNuovo);
        
        // Il turno è valido solo se il riposo è almeno di 11 ore
        return oreDiStacco >= 11;
    }
}