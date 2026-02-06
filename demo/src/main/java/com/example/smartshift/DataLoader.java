package com.example.smartshift;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

// Lo usiamo per caricare i dati
@Component
public class DataLoader {

    @Bean
    CommandLineRunner loadData(DipendenteRepository dipendenteRepository, AssenzaRepository assenzaRepository) {
        return args -> {
            // Controlla se il database è vuoto
            if (dipendenteRepository.count() == 0) {
                System.out.println("Database vuoto: Caricamento dati di prova...");
                
                // Creiamo i dipendenti
                Dipendente rossi = new Dipendente("Mario", "Rossi", 40, 8);
                Dipendente verdi = new Dipendente("Luigi", "Verdi", 20, 4);
                Dipendente bianchi = new Dipendente("Anna", "Bianchi", 30, 6);
                Dipendente beccarelli = new Dipendente("Raissa", "Beccarelli", 24, 6);
                Dipendente locatelli = new Dipendente("Giacomo", "Locatelli", 20, 5);
                Dipendente valceschini = new Dipendente("Marco", "Valceschini", 30, 5);

                // Salviamo i dipendenti (Spring restituisce l'oggetto salvato con l'ID vero)
                rossi = dipendenteRepository.save(rossi);
                verdi = dipendenteRepository.save(verdi);
                bianchi = dipendenteRepository.save(bianchi);
                beccarelli = dipendenteRepository.save(beccarelli);
                locatelli = dipendenteRepository.save(locatelli);
                valceschini = dipendenteRepository.save(valceschini);    

                // --- AGGIUNGIAMO UN'ASSENZA ---
                // Mandiamo Rossi in ferie il prossimo Lunedì (o domani, basta che sia nel range)
                LocalDate domani = LocalDate.now().plusDays(1); 
                
                // Assenza: Data, Tipo, Dipendente
                assenzaRepository.save(new Assenza(domani, "FERIE", rossi));

                System.out.println("Dati caricati!");
            }
        };
    }
}
