package com.example.smartshift.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

    @Column(name = "ferie_residue")
    private int ferieResidue = 26;

    @OneToMany(mappedBy = "dipendente", cascade = CascadeType.ALL)
    private List<Assenza> assenze = new ArrayList<>();

    public Dipendente() {}

    public Dipendente(String nome, String cognome, int oreSettimanaliContratto, int oreGiornaliereMax, int ferieResidue) {
        this.nome = nome;
        this.cognome = cognome;
        this.oreSettimanaliContratto = oreSettimanaliContratto;
        this.oreGiornaliereMax = oreGiornaliereMax;
        this.ferieResidue = ferieResidue;
    }

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
    public int getFerieResidue() { return ferieResidue; }
    public void setFerieResidue(int ferieResidue) { this.ferieResidue = ferieResidue; }
    public List<Assenza> getAssenze() { return assenze; }
    public void setAssenze(List<Assenza> assenze) { this.assenze = assenze; }
}