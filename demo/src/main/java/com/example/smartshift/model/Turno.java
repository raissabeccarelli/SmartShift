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

    private LocalDate data;
    private LocalTime oraInizio;
    private LocalTime oraFine;

    // ⚠️ QUI NON CI DEVE ESSERE @JsonIgnore !!!
    // Vogliamo vedere il dipendente nel JSON del turno
    @ManyToOne
    @JoinColumn(name = "dipendente_id")
    private Dipendente dipendente;

    public Turno() {}

    public Turno(LocalDate data, LocalTime oraInizio, LocalTime oraFine, Dipendente dipendente) {
        this.data = data;
        this.oraInizio = oraInizio;
        this.oraFine = oraFine;
        this.dipendente = dipendente;
    }

    // Getter e Setter standard
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