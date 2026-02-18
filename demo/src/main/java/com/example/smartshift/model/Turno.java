package com.example.smartshift.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Rappresenta un turno di lavoro assegnato a un dipendente
@Entity
@Table(name = "turni")
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;

    // Relazione con Dipendente
    @ManyToOne
    @JoinColumn(name = "dipendente_id")
    private Dipendente dipendente;

    // Costruttore vuoto
    public Turno() {}

    // Costruttore completo
    public Turno(LocalDate data, LocalTime oraInizio, LocalTime oraFine, Dipendente dipendente) {
        this.data = data;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.dipendente = dipendente;
    }

    // Getter e Setter
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