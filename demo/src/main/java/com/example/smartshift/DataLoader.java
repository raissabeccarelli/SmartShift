package com.example.smartshift;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.DipendenteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class DataLoader {

    @Bean
    CommandLineRunner loadData(DipendenteRepository repository) {
        return args -> {
            // Controlla se il database è vuoto per non duplicare i dati a ogni riavvio
            if (repository.count() == 0) {
                System.out.println("Database vuoto: Caricamento dati di prova...");

                // Creiamo 3 dipendenti con contratti diversi (come da input AlgoritmoTurni.txt)
                repository.save(new Dipendente("Mario", "Rossi", 40)); // Full time
                repository.save(new Dipendente("Luigi", "Verdi", 20)); // Part time
                repository.save(new Dipendente("Anna", "Bianchi", 30)); 

                System.out.println("Dati caricati! Dipendenti presenti: " + repository.count());
            }
        };
    }
}
