package com.example.smartshift.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dipendenti")
public class Dipendente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String cognome;

    @Column(name = "ore_settimanali_contratto")
    private int oreSettimanaliContratto;

    // --- NUOVO CAMPO ---
    @Column(name = "ore_giornaliere_max")
    private int oreGiornaliereMax; 
    // -------------------

    @OneToMany(mappedBy = "dipendente", cascade = CascadeType.ALL)
    private List<Assenza> assenze = new ArrayList<>();

    public Dipendente() {}

    // Costruttore aggiornato
    public Dipendente(String nome, String cognome, int oreSettimanaliContratto, int oreGiornaliereMax) {
        this.nome = nome;
        this.cognome = cognome;
        this.oreSettimanaliContratto = oreSettimanaliContratto;
        this.oreGiornaliereMax = oreGiornaliereMax; // Assegna il nuovo campo
    }

    // --- GETTER E SETTER NUOVI ---
    public int getOreGiornaliereMax() { return oreGiornaliereMax; }
    public void setOreGiornaliereMax(int oreGiornaliereMax) { this.oreGiornaliereMax = oreGiornaliereMax; }

    // (Lascia gli altri getter/setter come sono...)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }
    public int getOreSettimanaliContratto() { return oreSettimanaliContratto; }
    public void setOreSettimanaliContratto(int oreSettimanaliContratto) { this.oreSettimanaliContratto = oreSettimanaliContratto; }
}