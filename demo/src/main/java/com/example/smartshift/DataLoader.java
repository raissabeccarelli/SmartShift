package com.example.smartshift;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.DipendenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Lo usiamo per caricare i dati
@Component
public class DataLoader implements CommandLineRunner{
    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Override
    public void run(String... args) throws Exception {
        // se ci sono già dipendenti nel DB, mi fermo
        if (dipendenteRepository.count() > 0) {
            System.out.println("Database già popolato.");
            return;
        }

        // se siamo qui il DB è vuoto e quindi carico i dati
        System.out.println("Database vuoto. Carico dati...");

        dipendenteRepository.save(new Dipendente("Mario", "Rossi", 40, 8));
        dipendenteRepository.save(new Dipendente("Luigi", "Verdi", 20, 4)); // Part-time
        dipendenteRepository.save(new Dipendente("Anna", "Bianchi", 30, 6));

        System.out.println("Dati inseriti con successo!");
    }
}
