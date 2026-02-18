package com.example.smartshift.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/* Rappresenta un'assenza giornaliera di un dipendente
 * Ogni record identifica un singolo giorno di assenza
*/
@Entity
@Table(name = "assenze")
public class Assenza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data; // Giorno dell'assenza

    private String tipo; // Può essere ferie, permesso o malattia
    private String motivazione;

    // Relazione con Dipendente
    @ManyToOne
    @JoinColumn(name = "dipendente_id")
    @JsonIgnore
    private Dipendente dipendente;

    // Costruttore vuoto
    public Assenza() {}

    // Costruttore completo
    public Assenza(LocalDate data, String tipo, Dipendente dipendente, String motivazione) {
        this.data = data;
        this.tipo = tipo;
        this.dipendente = dipendente;
        this.motivazione = motivazione;
    }

    // Getter e Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Dipendente getDipendente() { return dipendente; }
    public void setDipendente(Dipendente dipendente) { this.dipendente = dipendente; }

    public String getMotivazione() { return motivazione; }
    public void setMotivazione(String motivazione) { this.motivazione = motivazione; }

    public Long getDipendenteId() {
    return (dipendente != null) ? dipendente.getId() : null;
}
}