package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;

@Service
public class TurnoService {

    @Autowired private DipendenteRepository dipendenteRepository;
    @Autowired private TurnoRepository turnoRepository;
    @Autowired private AssenzaRepository assenzaRepository;

    // INIEZIONE STRATEGY: Spring trova automaticamente le 3 regole create sopra
    @Autowired
    private List<ShiftRule> rules;

    // Classe interna per configurazione
    private static class ConfigurazioneSlot {
        LocalTime inizio;
        LocalTime fine;
        int minDipendenti;

        public ConfigurazioneSlot(int hInizio, int hFine, int minDipendenti) {
            this.inizio = LocalTime.of(hInizio, 0);
            this.fine = (hFine == 24) ? LocalTime.of(0, 0) : LocalTime.of(hFine, 0);
            this.minDipendenti = minDipendenti;
        }
    }

    // Configurazione predefinita aggiornata con il 4° slot (Notte)
    private List<ConfigurazioneSlot> currentConfig = new ArrayList<>(List.of(
        new ConfigurazioneSlot(8, 12, 3),   // Mattina
        new ConfigurazioneSlot(12, 16, 4),  // Pomeriggio
        new ConfigurazioneSlot(16, 20, 4),  // Sera
        new ConfigurazioneSlot(20, 24, 2)   // Notte
    ));

    public List<Map<String, Object>> getConfigurazioneSlot() {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

        for (ConfigurazioneSlot slot : currentConfig) {
            result.add(Map.of(
                "inizio", slot.inizio.format(formatter),
                "fine", slot.fine.format(formatter),
                "min", slot.minDipendenti
            ));
        }
        return result;
    }

    public void aggiungiAssenza(Long dipendenteId, LocalDate dataInizio, LocalDate dataFine, String tipo, String motivazione) {
        Dipendente d = dipendenteRepository.findById(dipendenteId)
                .orElseThrow(() -> new RuntimeException("Dipendente non trovato!"));

        if (dataFine == null) dataFine = dataInizio;
        long giorniRichiesti = java.time.temporal.ChronoUnit.DAYS.between(dataInizio, dataFine) + 1;

        if (tipo.equalsIgnoreCase("FERIE")) {
            if (d.getFerieResidue() < giorniRichiesti) {
                throw new RuntimeException("ERRORE: Ferie insufficienti! Hai " + d.getFerieResidue() + " giorni.");
            }
            d.setFerieResidue(d.getFerieResidue() - (int) giorniRichiesti);
            dipendenteRepository.save(d);
        }

        if (tipo.equalsIgnoreCase("PERMESSO") && (motivazione == null || motivazione.trim().isEmpty())) {
            throw new RuntimeException("ERRORE: Motivazione obbligatoria per i permessi!");
        }

        LocalDate current = dataInizio;
        while (!current.isAfter(dataFine)) {
            if(assenzaRepository.findByDipendenteAndData(d, current).isEmpty()) {
                assenzaRepository.save(new Assenza(current, tipo, d, motivazione));
            }
            current = current.plusDays(1);
        }
    }

    // --- METODO AGGIORNATO CON 4 PARAMETRI ---
    // ... imports e codice precedente ...

    @Transactional
    public void generaTurniPerSettimana(LocalDate dataInizio, int minMattina, int minPomeriggio, int minSera, int minNotte) {
        LocalDate dataFine = dataInizio.plusDays(6);

        turnoRepository.deleteTurniInRange(dataInizio, dataFine);
        turnoRepository.flush(); 

        // Recuperiamo tutti i dipendenti
        List<Dipendente> tuttiDipendenti = dipendenteRepository.findAll();

        List<ConfigurazioneSlot> slotsDinamici = List.of(
            new ConfigurazioneSlot(8, 12, minMattina),
            new ConfigurazioneSlot(12, 16, minPomeriggio),
            new ConfigurazioneSlot(16, 20, minSera),
            new ConfigurazioneSlot(20, 24, minNotte)
        );

        this.currentConfig = new ArrayList<>(slotsDinamici);

        for (LocalDate data = dataInizio; !data.isAfter(dataFine); data = data.plusDays(1)) {
            
            // Per ogni slot della giornata...
            for (ConfigurazioneSlot slot : slotsDinamici) {
                
                // --- LOGICA DI CONTINUITÀ ---
                // Dividiamo i dipendenti in due liste:
                // 1. "Prioritari": Chi ha finito un turno ESATTAMENTE quando inizia questo slot
                // 2. "Altri": Chi non sta lavorando o ha lavorato ore fa
                
                List<Dipendente> prioritari = new ArrayList<>();
                List<Dipendente> altri = new ArrayList<>(tuttiDipendenti); // Copia della lista completa

                // Cerchiamo chi è prioritario
                // Nota: È meglio farlo fuori dal sort per evitare troppe query al DB
                List<Dipendente> daSpostare = new ArrayList<>();
                
                for (Dipendente d : altri) {
                    if (haFinitoTurnoAlle(d, data, slot.inizio)) {
                        daSpostare.add(d);
                    }
                }
                
                // Rimuoviamo i prioritari dalla lista "altri" e li mettiamo in "prioritari"
                altri.removeAll(daSpostare);
                prioritari.addAll(daSpostare);

                // Mischiamo SOLO la lista "altri" per garantire rotazione tra chi non è in continuità
                Collections.shuffle(altri);

                // Costruiamo la lista finale ordinata: PRIMA chi deve continuare, POI gli altri
                List<Dipendente> dipendentiOrdinati = new ArrayList<>();
                dipendentiOrdinati.addAll(prioritari);
                dipendentiOrdinati.addAll(altri);

                // --- ASSEGNAZIONE ---
                int assegnati = 0;
                for (Dipendente d : dipendentiOrdinati) {
                    if (assegnati >= slot.minDipendenti) break;

                    // Il metodo tentaAssegnazioneTurno (che abbiamo modificato prima)
                    // controllerà se hanno ancora ore residue (es. 8h totali).
                    // Se il prioritario ha già fatto 8 ore, tornerà false e passerà al prossimo.
                    if (tentaAssegnazioneTurno(d, data, slot.inizio, slot.fine)) {
                        assegnati++;
                    }
                }
            }
        }
    }

    // --- NUOVO METODO HELPER ---
    // Controlla se il dipendente ha un turno che finisce all'ora specificata
    private boolean haFinitoTurnoAlle(Dipendente d, LocalDate data, LocalTime oraTarget) {
        List<Turno> turniOggi = turnoRepository.findByDipendenteAndData(d, data);
        for (Turno t : turniOggi) {
            // Se il turno finisce esattamente quando inizia il nuovo slot
            if (t.getOraFine().equals(oraTarget)) {
                return true;
            }
            // Gestione caso notturno (es. finisce a mezzanotte 00:00 e il target è 00:00)
            if (t.getOraFine().equals(LocalTime.MIDNIGHT) && oraTarget.equals(LocalTime.MIDNIGHT)) {
                return true;
            }
        }
        return false;
    }

    // ... il resto della classe (tentaAssegnazioneTurno, ecc.) rimane uguale ...

    // Metodo privato refattorizzato che usa le REGOLE
    private boolean tentaAssegnazioneTurno(Dipendente d, LocalDate data, LocalTime inizio, LocalTime fine) {
        
        // 1. Validazione Regole (Strategy)
        // La nuova SovrapposizioneRule ora permette due turni se non si toccano (es. 08-12 e 12-16)
        for (ShiftRule rule : rules) {
            if (!rule.isValid(d, data, inizio, fine)) {
                return false; 
            }
        }

        // --- CALCOLO DURATA SLOT ---
        int durataSlot = fine.getHour() - inizio.getHour();
        if (durataSlot < 0) durataSlot += 24; 
        if (durataSlot == 0 && fine.equals(LocalTime.of(0,0))) durataSlot = 24 - inizio.getHour();

        // --- CALCOLO ORE SETTIMANALI (Già esistente) ---
        LocalDate inizioSettimana = data.with(java.time.DayOfWeek.MONDAY);
        LocalDate fineSettimana = data.with(java.time.DayOfWeek.SUNDAY);
        List<Turno> turniSettimana = turnoRepository.findByDipendenteAndDataBetween(d, inizioSettimana, fineSettimana);
        
        int oreGiaLavorateSettimana = 0;
        int oreGiaLavorateOggi = 0; // <--- NUOVO CONTATORE

        for (Turno t : turniSettimana) {
            int h = t.getOraFine().getHour() - t.getOraInizio().getHour();
            if (h < 0) h += 24;
            
            oreGiaLavorateSettimana += h;

            // Se il turno è di OGGI, lo sommo al contatore giornaliero
            if (t.getData().equals(data)) {
                oreGiaLavorateOggi += h;
            }
        }

        // --- CALCOLO CAPACITÀ GIORNALIERA RESIDUA ---
        // Esempio: Elisa ha max 8h. Ha già fatto 4h oggi. 
        // residuoOggi = 8 - 4 = 4 ore disponibili ancora.
        int residuoOggi = d.getOreGiornaliereMax() - oreGiaLavorateOggi;
        
        if (residuoOggi <= 0) return false; // Ha già finito le ore per oggi

        // --- DEFINIZIONE ORE ASSEGNABILI ---
        // Deve essere il minimo tra:
        // 1. La durata dello slot (es. 4 ore)
        // 2. Quanto le manca al max giornaliero (es. 4 ore)
        // 3. Quanto le manca al max settimanale
        int oreDaAssegnare = Math.min(durataSlot, residuoOggi);
        oreDaAssegnare = Math.min(oreDaAssegnare, d.getOreSettimanaliContratto() - oreGiaLavorateSettimana);

        if (oreDaAssegnare <= 0) return false;

        // 3. Salvataggio
        LocalTime nuovaFine = inizio.plusHours(oreDaAssegnare);
        Turno nuovoTurno = new Turno(data, inizio, nuovaFine, d);
        turnoRepository.save(nuovoTurno);
        
        return true;
    }
}