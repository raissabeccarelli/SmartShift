package com.example.smartshift.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assenze")
public class Assenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data; // Giorno dell'assenza

    // Può essere "FERIE" o "PERMESSO"
    private String tipo; 

    @ManyToOne
    @JoinColumn(name = "dipendente_id")
    private Dipendente dipendente;

    // --- COSTRUTTORI ---
    public Assenza() {}

    public Assenza(LocalDate data, String tipo, Dipendente dipendente) {
        this.data = data;
        this.tipo = tipo;
        this.dipendente = dipendente;
    }

    // --- GETTER E SETTER ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Dipendente getDipendente() { return dipendente; }
    public void setDipendente(Dipendente dipendente) { this.dipendente = dipendente; }
}