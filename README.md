# SmartShift - Gestore Turni 

Sistema per la gestione e assegnazione dei turni del personale.

## Stato Attuale
Il progetto ha un **Backend funzionante** e un **Frontend Web** (HTML/JS) già integrato.

### Cosa Funziona:
1.  **Algoritmo di Assegnazione:**
    * Genera turni rispettando: riposo obbligatorio 11 ore, monte ore settimanale e disponibilità giornaliera (part-time).
    * Gestisce slot variabili (es. la mattina 3 dipendenti, la notte 1 dipendente).
    * Segnala in Console se una fascia è scoperta, ovvero se non gli sono stati assegnati abbastanza dipendenti.
2.  **Gestione Assenze Base:**
    * È possibile registrare ferie/malattie per un dipendente.
    * L'algoritmo "salta" automaticamente chi è assente durante la generazione.
3.  **Frontend:**
    * Tutto il frontend è in `src/main/resources/static`.
    * Generatore: pagina per lanciare l'algoritmo su una data specifica.
    * Calendario: visualizzazione tabellare dei turni generati.
    * Richieste: form per inserire ferie/malattie/permessi.

---

## Come Avviare
1.  Eseguire la classe `SmartShiftApplication.java`.
2.  Aprire il browser su: `http://localhost:8080`.
3.  Non serve installare altro (il DB è integrato/configurato).

---

## Prossimi passi
Queste sono le funzionalità da implementare nelle prossime iterazioni:

1.  **Storico e Versionamento:**
    * [ ] Salvare i turni raggruppati per "Settimana" (es. settimana 1, settimana 2) per poter navigare tra passato e futuro senza sovrascrivere tutto.
2.  **Assenze Avanzate:**
    * [ ] Aggiungere campo "Motivazione" per i permessi.
    * [ ] Gestione "Periodo di Ferie" (Range di date).
    * [ ] Controllo sul numero massimo di ferie annuali residue.
3.  **UI e Filtri:**
    * [ ] Possibilità di filtrare il calendario per vedere solo i turni di una specifica persona.
    * [ ] Alert Visivi: Evidenziare in ROSSO nel calendario le fasce che sono rimaste scoperte.
    * [ ] Modifica manuale dei turni generati (?).

---
