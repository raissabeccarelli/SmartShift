package com.example.smartshift.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class RiposoRuleTest {

    @Mock
    private TurnoRepository turnoRepository;

    @InjectMocks
    private RiposoRule riposoRule;

    @Test
    @DisplayName("Deve ritornare TRUE se non ci sono turni il giorno precedente")
    void testIsValid_NessunTurnoIeri_RitornaTrue() {
        Dipendente d = new Dipendente();
        LocalDate oggi = LocalDate.of(2023, 10, 20);
        LocalDate ieri = oggi.minusDays(1);

        when(turnoRepository.findByDipendenteAndData(d, ieri)).thenReturn(Collections.emptyList());

        boolean risultato = riposoRule.isValid(d, oggi, LocalTime.of(8, 0), LocalTime.of(12, 0));

        assertTrue(risultato, "Se non ha lavorato ieri, ha sicuramente riposato 11 ore.");
    }

    @Test
    @DisplayName("Deve ritornare FALSE se non sono passate 11 ore (Turno standard)")
    void testIsValid_RiposoInsufficiente_RitornaFalse() {
        Dipendente d = new Dipendente();
        LocalDate oggi = LocalDate.of(2023, 10, 20);
        LocalDate ieri = oggi.minusDays(1);

        Turno turnoIeri = new Turno(ieri, LocalTime.of(15, 0), LocalTime.of(23, 0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, ieri)).thenReturn(List.of(turnoIeri));

        boolean risultato = riposoRule.isValid(d, oggi, LocalTime.of(7, 0), LocalTime.of(13, 0));

        assertFalse(risultato, "Tra le 23:00 e le 07:00 ci sono solo 8 ore, regola violata.");
    }

    @Test
    @DisplayName("Deve ritornare FALSE se turno ieri ha scavallato la mezzanotte (Es. finito alle 02:00)")
    void testIsValid_ScavalcoMezzanotte_RitornaFalse() {
        Dipendente d = new Dipendente();
        LocalDate oggi = LocalDate.of(2023, 10, 20);
        LocalDate ieri = oggi.minusDays(1);
        Turno turnoNotturno = new Turno(ieri, LocalTime.of(18, 0), LocalTime.of(2, 0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, ieri)).thenReturn(List.of(turnoNotturno));
        boolean risultato = riposoRule.isValid(d, oggi, LocalTime.of(8, 0), LocalTime.of(14, 0));

        assertFalse(risultato, "Tra le 02:00 e le 08:00 ci sono 6 ore, regola violata.");
    }
    
    @Test
    @DisplayName("Deve ritornare TRUE se sono passate esattamente 11 ore")
    void testIsValid_LimiteEsatto_RitornaTrue() {
        Dipendente d = new Dipendente();
        LocalDate oggi = LocalDate.of(2023, 10, 20);
        LocalDate ieri = oggi.minusDays(1);
        Turno turnoIeri = new Turno(ieri, LocalTime.of(13, 0), LocalTime.of(21, 0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, ieri)).thenReturn(List.of(turnoIeri));
        boolean risultato = riposoRule.isValid(d, oggi, LocalTime.of(8, 0), LocalTime.of(14, 0));

        assertTrue(risultato, "11 ore esatte di stacco sono permesse.");
    }
}