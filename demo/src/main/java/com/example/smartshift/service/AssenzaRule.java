package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;

@Component
@Order(1) // Controlliamo prima le assenze (è veloce)
public class AssenzaRule implements ShiftRule {

    @Autowired
    private AssenzaRepository assenzaRepository;

    @Override
    public boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine) {
        return assenzaRepository.findByDipendenteAndData(dipendente, data).isEmpty();
    }
}