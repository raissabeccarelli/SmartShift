package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    @Autowired
    private List<ShiftRule> rules;

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

    private List<ConfigurazioneSlot> currentConfig = new ArrayList<>(List.of(
        new ConfigurazioneSlot(8, 12, 3),   
        new ConfigurazioneSlot(12, 16, 4),  
        new ConfigurazioneSlot(16, 20, 4),  
        new ConfigurazioneSlot(20, 24, 2)   
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
                throw new RuntimeException("ERRORE: Ferie insufficienti!");
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

    @Transactional
    public void generaTurniPerSettimana(LocalDate dataInizio, int minMattina, int minPomeriggio, int minSera, int minNotte) {
        LocalDate dataFine = dataInizio.plusDays(6);
        
        // Pulizia turni esistenti nel range
        turnoRepository.deleteTurniInRange(dataInizio, dataFine);
        turnoRepository.flush(); 

        List<Dipendente> tuttiDipendenti = dipendenteRepository.findAll();

        List<ConfigurazioneSlot> slotsDinamici = List.of(
            new ConfigurazioneSlot(8, 12, minMattina),
            new ConfigurazioneSlot(12, 16, minPomeriggio),
            new ConfigurazioneSlot(16, 20, minSera),
            new ConfigurazioneSlot(20, 24, minNotte)
        );

        this.currentConfig = new ArrayList<>(slotsDinamici);

        for (LocalDate data = dataInizio; !data.isAfter(dataFine); data = data.plusDays(1)) {
            
            // Risoluzione errore: creiamo una costante per la data del ciclo corrente
            final LocalDate dataCorrente = data; 

            for (ConfigurazioneSlot slot : slotsDinamici) {
                
                List<Dipendente> prioritari = new ArrayList<>();
                List<Dipendente> altri = new ArrayList<>();
                
                for (Dipendente d : tuttiDipendenti) {
                    // Controllo chi ha finito il turno esattamente all'inizio del nuovo slot
                    if (haFinitoTurnoAlle(d, dataCorrente, slot.inizio)) {
                        prioritari.add(d);
                    } else {
                        altri.add(d);
                    }
                }

                // --- ORDINAMENTO EQUO ---
                // Usiamo dataCorrente perché è all'interno di una Lambda
                altri.sort((d1, d2) -> {
                    double perc1 = getPercentualeLavoroSettimanale(d1, dataInizio, dataCorrente);
                    double perc2 = getPercentualeLavoroSettimanale(d2, dataInizio, dataCorrente);
                    return Double.compare(perc1, perc2);
                });

                // Uniamo le liste: prima chi deve dare continuità, poi chi ha lavorato meno
                List<Dipendente> dipendentiOrdinati = new ArrayList<>();
                dipendentiOrdinati.addAll(prioritari);
                dipendentiOrdinati.addAll(altri);

                int assegnati = 0;
                for (Dipendente d : dipendentiOrdinati) {
                    if (assegnati >= slot.minDipendenti) break;

                    // Tenta l'inserimento verificando regole e ore contrattuali
                    if (tentaAssegnazioneTurno(d, dataCorrente, slot.inizio, slot.fine)) {
                        assegnati++;
                    }
                }
            }
        }
    }
    
    private double getPercentualeLavoroSettimanale(Dipendente d, LocalDate lunedi, LocalDate dataCorrente) {
        List<Turno> turni = turnoRepository.findByDipendenteAndDataBetween(d, lunedi, dataCorrente);
        double oreFatte = turni.stream()
                .mapToDouble(t -> calcolaDurataOre(t.getOraInizio(), t.getOraFine()))
                .sum();
        
        return oreFatte / (double) d.getOreSettimanaliContratto();
    }

    private double calcolaDurataOre(LocalTime inizio, LocalTime fine) {
        long minuti = java.time.temporal.ChronoUnit.MINUTES.between(inizio, fine);
        if (fine.isBefore(inizio) || fine.equals(LocalTime.MIDNIGHT) && !inizio.equals(LocalTime.MIDNIGHT)) {
            minuti += 1440;
        }
        return minuti / 60.0;
    }

    private boolean haFinitoTurnoAlle(Dipendente d, LocalDate data, LocalTime oraTarget) {
        List<Turno> turniOggi = turnoRepository.findByDipendenteAndData(d, data);
        for (Turno t : turniOggi) {
            if (t.getOraFine().equals(oraTarget)) return true;
        }
        return false;
    }

    @Transactional
    boolean tentaAssegnazioneTurno(Dipendente d, LocalDate data, LocalTime inizio, LocalTime fine) {
        // 1. Validazione Regole Strategy
        for (ShiftRule rule : rules) {
            if (!rule.isValid(d, data, inizio, fine)) return false; 
        }

        // 2. Calcolo Carico Attuale dal DB
        double durataSlotRichiesta = calcolaDurataOre(inizio, fine);
        LocalDate lunedi = data.with(java.time.DayOfWeek.MONDAY);
        List<Turno> turniSettimana = turnoRepository.findByDipendenteAndDataBetween(d, lunedi, data);
        
        double oreLavorateSettimana = 0;
        double oreLavorateOggi = 0;

        for (Turno t : turniSettimana) {
            double h = calcolaDurataOre(t.getOraInizio(), t.getOraFine());
            oreLavorateSettimana += h;
            if (t.getData().equals(data)) oreLavorateOggi += h;
        }

        // 3. Verifica Limiti (Full-Time vs Part-Time)
        double residuoOggi = (double) d.getOreGiornaliereMax() - oreLavorateOggi;
        double residuoSettimana = (double) d.getOreSettimanaliContratto() - oreLavorateSettimana;

        if (residuoOggi <= 0 || residuoSettimana <= 0) return false;

        double oreEffettive = Math.min(durataSlotRichiesta, residuoOggi);
        oreEffettive = Math.min(oreEffettive, residuoSettimana);

        if (oreEffettive <= 0) return false;

        // 4. Salvataggio
        LocalTime nuovaFine = inizio.plusMinutes((long) (oreEffettive * 60));
        turnoRepository.save(new Turno(data, inizio, nuovaFine, d));
        
        return true;
    }
}