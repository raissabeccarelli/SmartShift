package com.example.smartshift.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DipendenteTest {

    @Test
    @DisplayName("Costruttore vuoto: deve rispettare i valori di default (26 ferie, lista non null)")
    void testCostruttoreVuoto_DefaultValues() {
        Dipendente d = new Dipendente();

        assertEquals(26, d.getFerieResidue(), "Il valore di default delle ferie deve essere 26");
        assertNotNull(d.getAssenze(), "La lista delle assenze non deve mai essere null appena creato l'oggetto");
        assertTrue(d.getAssenze().isEmpty(), "La lista delle assenze deve essere vuota ma inizializzata");
    }

    @Test
    @DisplayName("Costruttore completo: deve assegnare correttamente tutti i campi")
    void testCostruttoreCompleto() {
        String nome = "Mario";
        String cognome = "Rossi";
        int oreSettimanali = 40;
        int oreGiornaliere = 8;
        int ferie = 30;

        Dipendente d = new Dipendente(nome, cognome, oreSettimanali, oreGiornaliere, ferie);

        assertEquals(nome, d.getNome());
        assertEquals(cognome, d.getCognome());
        assertEquals(oreSettimanali, d.getOreSettimanaliContratto());
        assertEquals(oreGiornaliere, d.getOreGiornaliereMax());
        assertEquals(ferie, d.getFerieResidue(), "Il costruttore deve sovrascrivere il valore di default");
    }

    @Test
    @DisplayName("Setters e Getters: verifica funzionamento base")
    void testSettersAndGetters() {
        Dipendente d = new Dipendente();
        
        d.setId(100L);
        d.setNome("Luigi");
        d.setCognome("Verdi");
        d.setOreSettimanaliContratto(20);
        d.setOreGiornaliereMax(4);
        d.setFerieResidue(10);

        assertEquals(100L, d.getId());
        assertEquals("Luigi", d.getNome());
        assertEquals("Verdi", d.getCognome());
        assertEquals(20, d.getOreSettimanaliContratto());
        assertEquals(4, d.getOreGiornaliereMax());
        assertEquals(10, d.getFerieResidue());
    }

    @Test
    @DisplayName("Gestione Lista Assenze: deve essere possibile aggiungere elementi")
    void testListaAssenze() {
        Dipendente d = new Dipendente();
        Assenza a1 = new Assenza();
        Assenza a2 = new Assenza();
        
        List<Assenza> nuoveAssenze = new ArrayList<>();
        nuoveAssenze.add(a1);
        nuoveAssenze.add(a2);

        d.setAssenze(nuoveAssenze);

        assertEquals(2, d.getAssenze().size());
        assertTrue(d.getAssenze().contains(a1));
    }
    
    @Test
    @DisplayName("Correttezza logica ID (Sanity Check)")
    void testIdNullIniziale() {
        Dipendente d = new Dipendente();
        assertNull(d.getId(), "L'ID deve essere null prima che JPA lo assegni");
    }
}