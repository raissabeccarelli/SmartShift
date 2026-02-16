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

@Component
@Order(3) // Infine il controllo più complesso (11 ore riposo)
public class RiposoRule implements ShiftRule {

    @Autowired
    private TurnoRepository turnoRepository;

    @Override
    public boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine) {
        // Cerchiamo turni nel giorno precedente
        List<Turno> turniIeri = turnoRepository.findByDipendenteAndData(dipendente, data.minusDays(1));
        
        if (turniIeri.isEmpty()) return true;

        Turno ultimoTurnoIeri = turniIeri.get(turniIeri.size() - 1); 

        LocalDateTime fineIeri = LocalDateTime.of(ultimoTurnoIeri.getData(), ultimoTurnoIeri.getOraFine());
        
        // Se il turno finiva dopo mezzanotte (es. 22:00 - 06:00)
        if (ultimoTurnoIeri.getOraFine().isBefore(ultimoTurnoIeri.getOraInizio())) {
            fineIeri = fineIeri.plusDays(1);
        }

        LocalDateTime inizioNuovo = LocalDateTime.of(data, inizio);
        long oreDiStacco = ChronoUnit.HOURS.between(fineIeri, inizioNuovo);
        
        return oreDiStacco >= 11;
    }
}