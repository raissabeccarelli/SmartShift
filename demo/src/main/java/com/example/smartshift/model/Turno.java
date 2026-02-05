package com.example.smartshift.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turni")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data; // Giorno specifico (es. 2023-10-27)
    
    @Column(name = "ora_inizio")
    private LocalTime oraInizio;
    
    @Column(name = "ora_fine")
    private LocalTime oraFine;

    @ManyToOne
    @JoinColumn(name = "dipendente_id")
    private Dipendente dipendente; // Il dipendente assegnato 

    // --- COSTRUTTORI ---
    public Turno() {}

    public Turno(LocalDate data, LocalTime oraInizio, LocalTime oraFine, Dipendente dipendente) {
        this.data = data;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.dipendente = dipendente;
    }

    // --- GETTER E SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getOraInizio() { return oraInizio; }
    public void setOraInizio(LocalTime oraInizio) { this.oraInizio = oraInizio; }

    public LocalTime getOraFine() { return oraFine; }
    public void setOraFine(LocalTime oraFine) { this.oraFine = oraFine; }

    public Dipendente getDipendente() { return dipendente; }
    public void setDipendente(Dipendente dipendente) { this.dipendente = dipendente; }
}