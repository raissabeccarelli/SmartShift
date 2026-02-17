package com.example.smartshift.service;

// Import per JUnit 5
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

// Import per Mockito
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

// Import delle tue classi (Modelli e Repository)
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.TurnoRepository;

// Import Java Time e Utility
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
        // Arrange
        Dipendente d = new Dipendente();
        LocalDate data = LocalDate.now();
        // Simuliamo un turno esistente dalle 08:00 alle 12:00
        Turno esistente = new Turno(data, LocalTime.of(8,0), LocalTime.of(12,0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, data)).thenReturn(List.of(esistente));

        // Act
        // Nuovo turno dalle 10:00 alle 14:00 (si sovrappone!)
        boolean result = rule.isValid(d, data, LocalTime.of(10,0), LocalTime.of(14,0));
        
        // Assert
        assertFalse(result, "Dovrebbe fallire: il turno si sovrappone!");
    }

    @Test
    void testSovrapposizionePositiva() {
        // Test di controllo: un turno che non si sovrappone (es. pomeriggio)
        Dipendente d = new Dipendente();
        LocalDate data = LocalDate.now();
        Turno esistente = new Turno(data, LocalTime.of(8,0), LocalTime.of(12,0), d);
        
        when(turnoRepository.findByDipendenteAndData(d, data)).thenReturn(List.of(esistente));

        // Nuovo turno dalle 14:00 alle 18:00 (valido!)
        boolean result = rule.isValid(d, data, LocalTime.of(14,0), LocalTime.of(18,0));
        
        assertTrue(result, "Dovrebbe essere valido: non c'è sovrapposizione.");
    }
}