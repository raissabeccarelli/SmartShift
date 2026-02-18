package com.example.smartshift.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TurnoTest {

    @Test
    @DisplayName("Il costruttore completo deve inizializzare correttamente tutti i campi")
    void testCostruttoreCompleto() {
        LocalDate data = LocalDate.of(2023, 11, 1);
        LocalTime inizio = LocalTime.of(9, 0);
        LocalTime fine = LocalTime.of(18, 0);
        Dipendente d = new Dipendente("Mario", "Rossi", 40, 8, 26);

        Turno turno = new Turno(data, inizio, fine, d);

        assertNull(turno.getId(), "L'ID deve essere null prima del salvataggio DB");
        assertEquals(data, turno.getData());
        assertEquals(inizio, turno.getOraInizio());
        assertEquals(fine, turno.getOraFine());
        assertEquals(d, turno.getDipendente());
    }

    @Test
    @DisplayName("Setters e Getters devono funzionare correttamente")
    void testSettersAndGetters() {
        Turno turno = new Turno();
        LocalDate data = LocalDate.now();
        LocalTime inizio = LocalTime.of(8, 0);
        LocalTime fine = LocalTime.of(12, 0);
        Dipendente d = new Dipendente();
        d.setId(5L);

        turno.setId(100L);
        turno.setData(data);
        turno.setOraInizio(inizio);
        turno.setOraFine(fine);
        turno.setDipendente(d);

        assertEquals(100L, turno.getId());
        assertEquals(data, turno.getData());
        assertEquals(inizio, turno.getOraInizio());
        assertEquals(fine, turno.getOraFine());
        assertEquals(5L, turno.getDipendente().getId());
    }

    @Test
    @DisplayName("Il modello deve supportare turni che scavalcano la mezzanotte")
    void testTurnoNotturno() {
        // Verifica che sia possibile inserire un turno che va al di fuori delle configurazioni impostate
        Turno turno = new Turno();
        LocalTime inizio = LocalTime.of(22, 0);
        LocalTime fine = LocalTime.of(6, 0);

        turno.setOraInizio(inizio);
        turno.setOraFine(fine);

        assertEquals(22, turno.getOraInizio().getHour());
        assertEquals(6, turno.getOraFine().getHour());
    }
}