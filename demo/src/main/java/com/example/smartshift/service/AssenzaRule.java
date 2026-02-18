package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;

/*
 * Regola di validazione che verifica se un dipendente è assente
 * nel giorno in cui gli si vuole assegnare un turno
 * Implementa l'interfaccia ShiftRule
 */
@Component
@Order(1) // Priorità alta: controlliamo prima le assenze
public class AssenzaRule implements ShiftRule {

    // Repository utilizzato per verificare le assenze registrate
    @Autowired
    private AssenzaRepository assenzaRepository;

    // Controllo di validità del turno, ossia se non esiste un'assenza
    // per tale dipendente il giorno del turno
    @Override
    public boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine) {
        return assenzaRepository.findByDipendenteAndData(dipendente, data).isEmpty();
    }
}