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

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;

@ExtendWith(MockitoExtension.class)
class AssenzaRuleTest {

    @Mock
    private AssenzaRepository assenzaRepository;

    @InjectMocks
    private AssenzaRule assenzaRule;

    @Test
    @DisplayName("Dovrebbe restituire FALSE se il dipendente ha un'assenza registrata in quella data")
    void testIsValid_ConAssenza_RitornaFalse() {
        Dipendente dipendente = new Dipendente();
        dipendente.setId(1L);
        dipendente.setNome("Mario");
        
        LocalDate dataTurno = LocalDate.of(2023, 10, 25);
        Assenza assenzaFerie = new Assenza(dataTurno, "FERIE", dipendente, "Vacanza");
    
        when(assenzaRepository.findByDipendenteAndData(dipendente, dataTurno))
            .thenReturn(List.of(assenzaFerie));

        boolean risultato = assenzaRule.isValid(dipendente, dataTurno, LocalTime.of(9, 0), LocalTime.of(18, 0));
        assertFalse(risultato, "Il metodo dovrebbe restituire false perché c'è un'assenza.");
    }

    @Test
    @DisplayName("Dovrebbe restituire TRUE se non ci sono assenze per quella data")
    void testIsValid_SenzaAssenza_RitornaTrue() {
        Dipendente dipendente = new Dipendente();
        dipendente.setId(2L);
        
        LocalDate dataTurno = LocalDate.of(2023, 10, 26);

        when(assenzaRepository.findByDipendenteAndData(dipendente, dataTurno))
            .thenReturn(Collections.emptyList());

        boolean risultato = assenzaRule.isValid(dipendente, dataTurno, LocalTime.of(9, 0), LocalTime.of(18, 0));

        assertTrue(risultato, "Il metodo dovrebbe restituire true perché il dipendente è disponibile.");
    }
}