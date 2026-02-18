package com.example.smartshift.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled; // 1. IMPORTA LA REPOSITORY
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;

/*
 * Servizio schedulato che gestisce il reset annuale delle ferie, 
 * eseguito automaticamente il 1° gennaio ripristinando
 * il monte ferie annuale per tutti i dipendenti
 */
@Service
public class FerieScheduler {

    // Repository per aggiornare i dipendenti
    @Autowired
    private DipendenteRepository dipendenteRepository;

    // Repository per gestire le assenze (elimina lo storico)
    @Autowired
    private AssenzaRepository assenzaRepository;

    // Metodo scheduate che segue il reset il 1° Gennaio alle 00:00:00 CET
    @Scheduled(cron = "0 0 0 1 1 *", zone = "Europe/Rome")
    @Transactional
    public void resetFerieAnnuali() {
        System.out.println("🔄 ESECUZIONE RESET ANNUALE: Pulizia assenze e ripristino ferie...");

        // Cancella tutte le assenze da DB
        // Il contatore delle ferie godute torna a 0 per tutti i dipendenti
        assenzaRepository.deleteAll();

        // Ripristina il monte ferie annuale (26 giorni) per ogni dipendente
        List<Dipendente> dipendenti = dipendenteRepository.findAll();
        for (Dipendente d : dipendenti) {
            d.setFerieResidue(26);
        }

        // Salvataggio delle modifiche
        dipendenteRepository.saveAll(dipendenti);
        System.out.println("✅ Reset completato: Storico svuotato e 26 giorni assegnati.");
    }
}