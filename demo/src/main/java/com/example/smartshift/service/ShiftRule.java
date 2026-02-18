package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.smartshift.model.Dipendente;

/*
 * Interfaccia che definisce una regola di validazione
 * per l'assegnazione di un turno
 * Ciacuna delle implementazion rappresenta un vincolo specifico:
 * controllo assenze, sovrapposizione dei turni e riposo minimo
 *
 * Nuove regole possono essere aggiunte senza modificare
 * la logica principale di generazione turni
*/
public interface ShiftRule {
    // Verifica se il turno proposto è valido per il dipendente 
    // nella data e nell'intervallo orario specificato
    boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine);
}