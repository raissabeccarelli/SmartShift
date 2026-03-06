# SmartShift

SmartShift è un'applicazione backend sviluppata in **Java e Spring Boot** per la gestione intelligente e automatizzata dei turni di lavoro dei dipendenti. 

Il sistema genera programmazioni settimanali rispettando vincoli complessi come il monte ore contrattuale, il riposo minimo tra un turno e l'altro, le assenze programmate e l'ottimizzazione degli orari (evitando i turni spezzati).

## Funzionalità Principali

* **Generazione Automatica dei Turni:** Crea un'intera settimana di turni coprendo il fabbisogno minimo di personale per ogni fascia oraria (Mattina, Pomeriggio, Sera, Notte).
* **Motore di Regole (Strategy Pattern):** Un sistema flessibile che valuta i vincoli prima di assegnare un turno:
    * Controllo del massimale di ore giornaliero (es. max 8h) e settimanale (es. 40h o part-time 20h).
    * Controllo delle assenze (ferie, permessi, malattie).
    * Rispetto delle 11 ore di riposo minimo consecutivo tra la fine di un turno e l'inizio del successivo.
    * Prevenzione delle sovrapposizioni.
* **Logica di Continuità:** Ottimizza gli slot accorpando i turni (es. 4h + 4h) per permettere ai dipendenti di raggiungere il proprio massimale giornaliero in un unico blocco, migliorando la qualità del lavoro.
* **Gestione Dipendenti & Assenze:** API REST complete per le operazioni CRUD sul personale e sulle loro indisponibilità.

## Stack Tecnologico

* **Linguaggio:** Java
* **Framework:** Spring Boot 3
* **Accesso ai Dati:** Spring Data JPA / Hibernate
* **Database:** SQLite (basato su file locale)
* **Build Tool:** Apache Maven
* **Qualità del Codice & Analisi Statica:** JaCoCo (Coverage), SpotBugs (Bytecode Analysis), PMD (Source Code Analysis), JDepend (Design Quality)
