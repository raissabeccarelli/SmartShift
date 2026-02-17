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
        // 1. ARRANGE (Preparazione dati)
        Dipendente dipendente = new Dipendente();
        dipendente.setId(1L);
        dipendente.setNome("Mario");
        
        LocalDate dataTurno = LocalDate.of(2023, 10, 25);
        
        // Creiamo un'assenza finta
        Assenza assenzaFerie = new Assenza(dataTurno, "FERIE", dipendente, "Vacanza");
        
        // Istruiamo il Mock: quando cerchi le assenze per Mario il 25/10, restituisci una lista che contiene le ferie
        when(assenzaRepository.findByDipendenteAndData(dipendente, dataTurno))
            .thenReturn(List.of(assenzaFerie));

        // 2. ACT (Esecuzione)
        // Gli orari non sono importanti per questa regola, mettiamo valori a caso
        boolean risultato = assenzaRule.isValid(dipendente, dataTurno, LocalTime.of(9, 0), LocalTime.of(18, 0));

        // 3. ASSERT (Verifica)
        assertFalse(risultato, "Il metodo dovrebbe restituire false perché c'è un'assenza.");
    }

    @Test
    @DisplayName("Dovrebbe restituire TRUE se non ci sono assenze per quella data")
    void testIsValid_SenzaAssenza_RitornaTrue() {
        // 1. ARRANGE
        Dipendente dipendente = new Dipendente();
        dipendente.setId(2L);
        
        LocalDate dataTurno = LocalDate.of(2023, 10, 26);

        // Istruiamo il Mock: restituisci una lista VUOTA (nessuna assenza trovata)
        when(assenzaRepository.findByDipendenteAndData(dipendente, dataTurno))
            .thenReturn(Collections.emptyList());

        // 2. ACT
        boolean risultato = assenzaRule.isValid(dipendente, dataTurno, LocalTime.of(9, 0), LocalTime.of(18, 0));

        // 3. ASSERT
        assertTrue(risultato, "Il metodo dovrebbe restituire true perché il dipendente è disponibile.");
    }
}