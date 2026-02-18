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

    @Spy private ArrayList<ShiftRule> rules = new ArrayList<>();

    @InjectMocks private TurnoService turnoService;

    @BeforeEach
    void setup() {
        rules.add((d, data, in, fine) -> true);
    }

    @Test
    @DisplayName("getConfigurazioneSlot: deve restituire i 4 slot standard")
    void testGetConfigurazioneSlot() {
        List<Map<String, Object>> config = turnoService.getConfigurazioneSlot();
        
        assertNotNull(config);
        assertEquals(4, config.size());
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
        LocalDate fine = LocalDate.of(2023, 10, 2);

        when(assenzaRepository.findByDipendenteAndData(eq(d), any(LocalDate.class)))
            .thenReturn(Collections.emptyList());

        turnoService.aggiungiAssenza(dipId, inizio, fine, "MALATTIA", "Influenza");
        verify(assenzaRepository, times(2)).save(any(Assenza.class));
    }

    @Test
    @DisplayName("aggiungiAssenza: Errore Ferie Insufficienti")
    void testAggiungiAssenza_FerieInsufficienti() {
        Long dipId = 1L;
        Dipendente d = new Dipendente("Mario", "Rossi", 40, 8, 2);
        when(dipendenteRepository.findById(dipId)).thenReturn(Optional.of(d));
        
        LocalDate inizio = LocalDate.of(2023, 10, 1);
        LocalDate fine = LocalDate.of(2023, 10, 5);
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
        assertThrows(RuntimeException.class, () -> {
            turnoService.aggiungiAssenza(dipId, LocalDate.now(), null, "PERMESSO", "");
        });
    }
    
    @Test
    @DisplayName("generaTurniPerSettimana: Happy Path (Verifica flusso completo)")
    void testGeneraTurniPerSettimana_HappyPath() {
        LocalDate start = LocalDate.of(2023, 10, 23);
        
        Dipendente d1 = new Dipendente("Mario", "Rossi", 40, 8, 26);
        d1.setId(1L);
        List<Dipendente> dipendenti = List.of(d1);

        when(dipendenteRepository.findAll()).thenReturn(dipendenti);
        when(turnoRepository.findByDipendenteAndData(any(), any())).thenReturn(Collections.emptyList());
        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any())).thenReturn(Collections.emptyList());

        turnoService.generaTurniPerSettimana(start, 1, 1, 1, 1);

        verify(turnoRepository).deleteTurniInRange(eq(start), any(LocalDate.class));
        verify(turnoRepository, atLeastOnce()).save(any(Turno.class));
    }

    @Test
    @DisplayName("tentaAssegnazioneTurno: Tronca il turno se supera il limite giornaliero (Part-Time)")
    void testTentaAssegnazione_TroncaTurnoPartTime() throws Exception {
        Dipendente partTime = new Dipendente("Luigi", "Verdi", 20, 4, 26);
        partTime.setId(1L);
        LocalDate oggi = LocalDate.of(2023, 10, 23);
        LocalTime inizio = LocalTime.of(8, 0);
        LocalTime fine = LocalTime.of(14, 0);

        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any()))
            .thenReturn(Collections.emptyList());

        boolean risultato = invokePrivateMethod(turnoService, "tentaAssegnazioneTurno", partTime, oggi, inizio, fine);

        assertTrue(risultato);
        verify(turnoRepository).save(argThat(t -> t.getOraFine().equals(LocalTime.of(12, 0))));
    }

    @Test
    @DisplayName("tentaAssegnazioneTurno: Tronca se rimangono poche ore settimanali")
    void testTentaAssegnazione_TroncaPerLimiteSettimanale() throws Exception {
        Dipendente fullTime = new Dipendente("Mario", "Rossi", 40, 8, 26);
        LocalDate oggi = LocalDate.of(2023, 10, 27);
        LocalTime inizio = LocalTime.of(8, 0);
        LocalTime fine = LocalTime.of(16, 0);
        List<Turno> turniGiaFatti = new ArrayList<>();
        LocalDate lunedi = oggi.with(java.time.DayOfWeek.MONDAY);

        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime));
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime));
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime));
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(16,0), fullTime));
        turniGiaFatti.add(new Turno(lunedi, LocalTime.of(8,0), LocalTime.of(14,0), fullTime));

        when(turnoRepository.findByDipendenteAndDataBetween(any(), any(), any())).thenReturn(turniGiaFatti);

        boolean risultato = invokePrivateMethod(turnoService, "tentaAssegnazioneTurno", fullTime, oggi, inizio, fine);

        assertTrue(risultato);
        verify(turnoRepository).save(argThat(t -> t.getOraFine().equals(LocalTime.of(10, 0))));
    }

    @Test
    @DisplayName("tentaAssegnazioneTurno: Rifiuta se ore esaurite")
    void testTentaAssegnazione_RifiutaSeOreEsaurite() throws Exception {
        Dipendente fullTime = new Dipendente("Mario", "Rossi", 40, 8, 26);
        LocalDate oggi = LocalDate.of(2023, 10, 27);
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

    private boolean invokePrivateMethod(Object target, String methodName, Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, Dipendente.class, LocalDate.class, LocalTime.class, LocalTime.class);
        method.setAccessible(true);
        return (boolean) method.invoke(target, args);
    }
}