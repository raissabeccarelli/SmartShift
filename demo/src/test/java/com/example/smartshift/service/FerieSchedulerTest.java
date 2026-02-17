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

    // 3. Definiamo il Captor per catturare liste/iterable di dipendenti
    @Captor
    private ArgumentCaptor<Iterable<Dipendente>> dipendentiCaptor;

    @Test
    @DisplayName("Il reset annuale deve cancellare lo storico e reimpostare 26 giorni per tutti")
    void testResetFerieAnnuali() {
        // ARRANGE
        Dipendente d1 = new Dipendente();
        d1.setNome("Mario");
        d1.setFerieResidue(5); 
        
        Dipendente d2 = new Dipendente();
        d2.setNome("Luigi");
        d2.setFerieResidue(0); 

        when(dipendenteRepository.findAll()).thenReturn(Arrays.asList(d1, d2));

        // ACT
        ferieScheduler.resetFerieAnnuali();

        // ASSERT
        verify(assenzaRepository, times(1)).deleteAll();

        // 4. CATTURIAMO l'argomento passato a saveAll
        verify(dipendenteRepository).saveAll(dipendentiCaptor.capture());
        
        // 5. Estraiamo il valore catturato e lo convertiamo in lista
        Iterable<Dipendente> valoreCatturato = dipendentiCaptor.getValue();
        List<Dipendente> listaSalvata = (List<Dipendente>) valoreCatturato; 

        // 6. Ora possiamo usare .contains e .get senza errori
        assertTrue(listaSalvata.contains(d1));
        assertTrue(listaSalvata.contains(d2));
        
        // Verifichiamo che le ferie siano state resettate nell'oggetto salvato
        assertEquals(26, listaSalvata.get(0).getFerieResidue());
        assertEquals(26, listaSalvata.get(1).getFerieResidue());
    }
}