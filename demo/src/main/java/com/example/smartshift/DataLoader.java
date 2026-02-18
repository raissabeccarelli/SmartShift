package com.example.smartshift;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.DipendenteRepository;

/*
 * Classe di inizializzazione dei dati che viene eseguita
 * all'avvio dell'applicazione per caricare i dati
*/
@Component
public class DataLoader implements CommandLineRunner{
    // Repository per i dipendenti
    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Override
    public void run(String... args) throws Exception {
        // Se ci sono già dipendenti nel DB evito di reinserirli
        if (dipendenteRepository.count() > 0) {
            System.out.println("Database già popolato.");
            return;
        }

        // Se il DB è vuoto inserisco i dati di test
        System.out.println("Database vuoto. Carico dati...");

        // Inserimento di dipendenti di test
        dipendenteRepository.save(new Dipendente("Mario", "Rossi", 40, 8, 26)); // Full-time
        dipendenteRepository.save(new Dipendente("Luigi", "Verdi", 20, 4, 26)); // Part-time
        dipendenteRepository.save(new Dipendente("Anna", "Bianchi", 30, 6, 26)); // Contratto ridotto

        System.out.println("Dati inseriti con successo!");
    }
}
