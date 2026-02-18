package com.example.smartshift.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AssenzaTest {

    @Test
    @DisplayName("Il costruttore deve inizializzare correttamente i campi")
    void testCostruttore() {
        LocalDate data = LocalDate.of(2023, 10, 15);
        String tipo = "FERIE";
        String motivazione = "Vacanza";
        Dipendente d = new Dipendente();
        d.setId(10L);

        Assenza assenza = new Assenza(data, tipo, d, motivazione);

        assertEquals(data, assenza.getData());
        assertEquals(tipo, assenza.getTipo());
        assertEquals(d, assenza.getDipendente());
        assertEquals(motivazione, assenza.getMotivazione());
    }

    @Test
    @DisplayName("Setters e Getters devono funzionare correttamente")
    void testSettersAndGetters() {
        Assenza assenza = new Assenza();
        LocalDate data = LocalDate.now();
        Dipendente d = new Dipendente();
        
        assenza.setId(1L);
        assenza.setData(data);
        assenza.setTipo("MALATTIA");
        assenza.setMotivazione("Influenza");
        assenza.setDipendente(d);

        assertEquals(1L, assenza.getId());
        assertEquals(data, assenza.getData());
        assertEquals("MALATTIA", assenza.getTipo());
        assertEquals("Influenza", assenza.getMotivazione());
        assertEquals(d, assenza.getDipendente());
    }

    @Test
    @DisplayName("getDipendenteId deve restituire l'ID se il dipendente esiste")
    void testGetDipendenteId_ConDipendente() {
        Dipendente d = new Dipendente();
        d.setId(99L);
        Assenza assenza = new Assenza();
        assenza.setDipendente(d);

        assertEquals(99L, assenza.getDipendenteId());
    }

    @Test
    @DisplayName("getDipendenteId deve restituire NULL se il dipendente è null (Null Safety)")
    void testGetDipendenteId_SenzaDipendente() {
        Assenza assenza = new Assenza();
        assenza.setDipendente(null);

        Long resultId = assenza.getDipendenteId();

        assertNull(resultId, "Il metodo non deve lanciare NullPointerException, ma restituire null");
    }
}