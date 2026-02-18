package com.example.smartshift.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.TurnoRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SovrapposizioneRuleTest {

    @Mock
    private TurnoRepository turnoRepository; 

    @InjectMocks
    private SovrapposizioneRule rule; 

    @Test
    void testSovrapposizioneNegativa() {
        Dipendente d = new Dipendente();
        LocalDate data = LocalDate.now();
        Turno esistente = new Turno(data, LocalTime.of(8,0), LocalTime.of(12,0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, data)).thenReturn(List.of(esistente));
        boolean result = rule.isValid(d, data, LocalTime.of(10,0), LocalTime.of(14,0));
        
        assertFalse(result, "Dovrebbe fallire: il turno si sovrappone!");
    }

    @Test
    void testSovrapposizionePositiva() {
        Dipendente d = new Dipendente();
        LocalDate data = LocalDate.now();
        Turno esistente = new Turno(data, LocalTime.of(8,0), LocalTime.of(12,0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, data)).thenReturn(List.of(esistente));
        boolean result = rule.isValid(d, data, LocalTime.of(14,0), LocalTime.of(18,0));
        
        assertTrue(result, "Dovrebbe essere valido: non c'è sovrapposizione.");
    }
}