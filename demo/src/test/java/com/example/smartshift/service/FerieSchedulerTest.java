package com.example.smartshift.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor; 
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;

@ExtendWith(MockitoExtension.class)
class FerieSchedulerTest {

    @Mock
    private DipendenteRepository dipendenteRepository;

    @Mock
    private AssenzaRepository assenzaRepository;

    @InjectMocks
    private FerieScheduler ferieScheduler;

    // Definiamo il Captor per catturare liste di dipendenti
    @Captor
    private ArgumentCaptor<Iterable<Dipendente>> dipendentiCaptor;

    @Test
    @DisplayName("Il reset annuale deve cancellare lo storico e reimpostare 26 giorni per tutti")
    void testResetFerieAnnuali() {
        Dipendente d1 = new Dipendente();
        d1.setNome("Mario");
        d1.setFerieResidue(5); 
        
        Dipendente d2 = new Dipendente();
        d2.setNome("Luigi");
        d2.setFerieResidue(0); 

        when(dipendenteRepository.findAll()).thenReturn(Arrays.asList(d1, d2));
        ferieScheduler.resetFerieAnnuali();

        verify(assenzaRepository, times(1)).deleteAll();

        verify(dipendenteRepository).saveAll(dipendentiCaptor.capture());

        Iterable<Dipendente> valoreCatturato = dipendentiCaptor.getValue();
        List<Dipendente> listaSalvata = (List<Dipendente>) valoreCatturato; 

        assertTrue(listaSalvata.contains(d1));
        assertTrue(listaSalvata.contains(d2));
        
        assertEquals(26, listaSalvata.get(0).getFerieResidue());
        assertEquals(26, listaSalvata.get(1).getFerieResidue());
    }
}