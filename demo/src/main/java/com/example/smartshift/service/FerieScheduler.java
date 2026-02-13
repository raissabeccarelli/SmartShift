package com.example.smartshift.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled; // 1. IMPORTA LA REPOSITORY
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;

@Service
public class FerieScheduler {

    @Autowired
    private DipendenteRepository dipendenteRepository;

    @Autowired
    private AssenzaRepository assenzaRepository; // 2. INIETTA LA REPOSITORY

    // Esegue il reset il 1° Gennaio alle 00:00:00
    @Scheduled(cron = "0 0 0 1 1 *", zone = "Europe/Rome")
    @Transactional
    public void resetFerieAnnuali() {
        System.out.println("🔄 ESECUZIONE RESET ANNUALE: Pulizia assenze e ripristino ferie...");

        // 3. CANCELLA TUTTE LE ASSENZE DAL DATABASE
        // In questo modo il contatore "Ferie Godute" tornerà a 0 per tutti
        assenzaRepository.deleteAll();

        // 4. RESET DELLE FERIE RESIDUE A 26
        List<Dipendente> dipendenti = dipendenteRepository.findAll();
        for (Dipendente d : dipendenti) {
            d.setFerieResidue(26);
        }

        dipendenteRepository.saveAll(dipendenti);
        System.out.println("✅ Reset completato: Storico svuotato e 26 giorni assegnati.");
    }
}