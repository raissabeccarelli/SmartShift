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

    @Column(name = "ore_giornaliere_max")
    private int oreGiornaliereMax;

    // Relazione: Un dipendente ha molte assenze
    // mappedBy = "dipendente" indica che la chiave esterna è nell'altra tabella
    @OneToMany(mappedBy = "dipendente", cascade = CascadeType.ALL)
    private List<Assenza> assenze = new ArrayList<>();

    // --- COSTRUTTORI ---
    
    // Costruttore vuoto (Obbligatorio per JPA/Hibernate)
    public Dipendente() {}

    // Costruttore COMPLETO (Usato nel DataLoader)
    public Dipendente(String nome, String cognome, int oreSettimanaliContratto, int oreGiornaliereMax) {
        this.nome = nome;
        this.cognome = cognome;
        this.oreSettimanaliContratto = oreSettimanaliContratto;
        this.oreGiornaliereMax = oreGiornaliereMax;
    }

    // --- GETTER E SETTER ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public int getOreSettimanaliContratto() { return oreSettimanaliContratto; }
    public void setOreSettimanaliContratto(int oreSettimanaliContratto) { this.oreSettimanaliContratto = oreSettimanaliContratto; }

    public int getOreGiornaliereMax() { return oreGiornaliereMax; }
    public void setOreGiornaliereMax(int oreGiornaliereMax) { this.oreGiornaliereMax = oreGiornaliereMax; }

    public List<Assenza> getAssenze() { return assenze; }
    public void setAssenze(List<Assenza> assenze) { this.assenze = assenze; }
}