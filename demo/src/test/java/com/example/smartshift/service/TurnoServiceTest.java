package com.example.smartshift.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock private DipendenteRepository dipendenteRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private AssenzaRepository assenzaRepository;

    // Spy sulla lista regole per poterla manipolare
    @Spy private ArrayList<ShiftRule> rules = new ArrayList<>();

    @InjectMocks private TurnoService turnoService;

    @BeforeEach
    void setup() {
        // Regola permissiva per non bloccare i test di logica matematica
        rules.add((d, data, in, fine) -> true);
    }

    // ===================================================================================
    // TEST METODI PUBBLICI (Configurazione & Assenze)
    // ===================================================================================

    @Test
    @DisplayName("getConfigurazioneSlot: deve restituire i 4 slot standard")
    void testGetConfigurazioneSlot() {
        List<Map<String, Object>> config = turnoService.getConfigurazioneSlot();
        
        assertNotNull(config);
        assertEquals(4, config.size());
        
        // Verifica il primo slot (Mattina 08:00 - 12:00)
        assertEquals("08:00", config.get(0).get("inizio"));
        assertEquals("12:00", config.get(0).get("fine"));
    }

    @Test
    @DisplayName("aggiungiAssenza: Successo (Malattia su più giorni)")
    void testAggiungiAssenza_Successo() {
        Long dipId = 1L;
        Dipendente d = new Dipendente("Mario", "Rossi", 40, 8, 26);
        when(dipendenteRepository.findById(dipId)).thenReturn(Optional.of(d));
        
        LocalDate inizio = LocalDate.of(2023, 10, 1);
        LocalDate fine = LocalDate.of(2023, 10, 2); // 2 giorni

        // Mockiamo che non ci siano assenze preesistenti
        when(assenzaRepository.findByDipendenteAndData(eq(d), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        // ACT
        turnoService.aggiungiAssenza(dipId, inizio, fine, "MALATTIA", "Influenza");

        // ASSERT
        // Deve salvare 2 entità assenza (una per il 1° ott, una per il 2° ott)
        verify(assenzaRepository, times(2)).save(any(Assenza.class));
    }

    @Test
    @DisplayName("aggiungiAssenza: Errore Ferie Insufficienti")
    void testAggiungiAssenza_FerieInsufficienti() {
        Long dipId = 1L;
        Dipendente d = new Dipendente("Mario", "Rossi", 40, 8, 2); // Solo 2 giorni residui
        when(dipendenteRepository.findById(dipId)).thenReturn(Optional.of(d));
        
        LocalDate inizio = LocalDate.of(2023, 10, 1);
        LocalDate fine = LocalDate.of(2023, 10, 5); // Richiesti 5 giorni

        // ACT & ASSERT
        Exception e = assertThrows(RuntimeException.class, () -> {
            turnoService.aggiungiAssenza(dipId, inizio, fine, "FERIE", "");
        });

        assertTrue(e.getMessage().contains("Ferie insufficienti"));
        verify(assenzaRepository, never()).save(any());
    }

    @Test
    @DisplayName("aggiungiAssenza: Errore Permesso senza motivazione")
    void testAggiungiAssenza_PermessoSenzaMotivazione() {
        Long dipId = 1L;
        Dipendente d = new Dipendente();
        when(dipendenteRepository.findById(dipId)).thenReturn(Optional.of(d));

        // ACT & ASSERT
        assertThrows(RuntimeException.class, () -> {
            turnoService.aggiungiAssenza(dipId, LocalDate.now(), null, "PERMESSO", ""); // Motivazione vuota
        });
    }

    // ===================================================================================
    // TEST GENERAZIONE COMPLETA (Integration-like)
    // ===================================================================================

    @Test
    @DisplayName("generaTurniPerSettimana: Happy Path (Verifica flusso completo)")
    void testGeneraTurniPerSettimana_HappyPath() {
        // ARRANGE
        LocalDate start = LocalDate.of(2023, 10, 23); // Lunedì
        
        Dipendente d1 = new Dipendente("Mario", "Rossi", 40, 8, 26);
        d1.setId(1L);
        List<Dipendente> dipendenti = List.of(d1);

        when(dipendenteRepository.findAll()).thenReturn(dipendenti);
        
        // Mock per evitare NullPointer durante i calcoli interni al ciclo
        // Restituiamo liste vuote per dire "nessun turno precedente, nessun conflitto"
        when(turnoRepository.findByDipendenteAndData(any(), any())).thenReturn(Collections.emptyList());
        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        // ACT
        turnoService.generaTurniPerSettimana(start, 1, 1, 1, 1);

        // ASSERT
        // 1. Verifica pulizia vecchia
        verify(turnoRepository).deleteTurniInRange(eq(start), any(LocalDate.class));
        
        // 2. Verifica che abbia provato a salvare dei turni
        // Dato che abbiamo 1 dipendente e chiediamo 1 persona per slot per 4 slot al giorno per 7 giorni...
        // ...al netto dei limiti orari (8h max), dovrebbe salvare qualcosa.
        verify(turnoRepository, atLeastOnce()).save(any(Turno.class));
    }

    // ===================================================================================
    // TEST METODI PRIVATI (Logica Assegnazione - testati via Reflection)
    // ===================================================================================

    @Test
    @DisplayName("tentaAssegnazioneTurno: Tronca il turno se supera il limite giornaliero (Part-Time)")
    void testTentaAssegnazione_TroncaTurnoPartTime() throws Exception {
        Dipendente partTime = new Dipendente("Luigi", "Verdi", 20, 4, 26);
        partTime.setId(1L);
        LocalDate oggi = LocalDate.of(2023, 10, 23);
        
        // Richiesta: 6 ore. Limite: 4 ore.
        LocalTime inizio = LocalTime.of(8, 0);
        LocalTime fine = LocalTime.of(14, 0);

        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any()))
            .thenReturn(Collections.emptyList());

        boolean risultato = invokePrivateMethod(turnoService, "tentaAssegnazioneTurno", partTime, oggi, inizio, fine);

        assertTrue(risultato);
        // Verifica salvataggio troncato alle 12:00
        verify(turnoRepository).save(argThat(t -> t.getOraFine().equals(LocalTime.of(12, 0))));
    }

    @Test
    @DisplayName("tentaAssegnazioneTurno: Tronca se rimangono poche ore settimanali")
    void testTentaAssegnazione_TroncaPerLimiteSettimanale() throws Exception {
        Dipendente fullTime = new Dipendente("Mario", "Rossi", 40, 8, 26);
        LocalDate oggi = LocalDate.of(2023, 10, 27);
        LocalTime inizio = LocalTime.of(8, 0);
        LocalTime fine = LocalTime.of(16, 0);

        // Simuliamo 38 ore già fatte
        List<Turno> turniGiaFatti = new ArrayList<>();
        LocalDate lunedi = oggi.with(java.time.DayOfWeek.MONDAY);
        // Aggiungiamo turni per arrivare a 38 ore
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime)); // 8
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime)); // 16
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime)); // 24
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime)); // 32
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(14,0), fullTime)); // 38

        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any())).thenReturn(turniGiaFatti);

        boolean risultato = invokePrivateMethod(turnoService, "tentaAssegnazioneTurno", fullTime, oggi, inizio, fine);

        assertTrue(risultato);
        // Deve salvare solo 2 ore (fino a 40), quindi 10:00
        verify(turnoRepository).save(argThat(t -> t.getOraFine().equals(LocalTime.of(10, 0))));
    }

    @Test
    @DisplayName("tentaAssegnazioneTurno: Rifiuta se ore esaurite")
    void testTentaAssegnazione_RifiutaSeOreEsaurite() throws Exception {
        Dipendente fullTime = new Dipendente("Mario", "Rossi", 40, 8, 26);
        LocalDate oggi = LocalDate.of(2023, 10, 27);
        
        // Simuliamo 40 ore già fatte
        List<Turno> turniGiaFatti = new ArrayList<>();
        LocalDate lunedi = oggi.with(java.time.DayOfWeek.MONDAY);
        for(int i=0; i<5; i++) {
            turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime));
        }

        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any())).thenReturn(turniGiaFatti);

        boolean risultato = invokePrivateMethod(turnoService, "tentaAssegnazioneTurno", fullTime, oggi, LocalTime.of(8,0), LocalTime.of(12,0));

        assertFalse(risultato);
        verify(turnoRepository, never()).save(any());
    }

    // --- Helper Reflection ---
    private boolean invokePrivateMethod(Object target, String methodName, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, Dipendente.class, LocalDate.class, LocalTime.class, LocalTime.class);
        method.setAccessible(true);
        return (boolean) method.invoke(target, args);
    }
}