package com.example.smartshift.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartshift.model.Dipendente;

/*
 * Repository per la gestione delle operazioni CRUD su Dipendente
 * Estende JpaRepository per ereditare metodi standard
 */
@Repository
public interface DipendenteRepository extends JpaRepository<Dipendente, Long> {

    /*
     * Recupera tutti i dipendenti con un dato nome
     * Query: SELECT * FROM dipendenti WHERE nome = ?
    */
    List<Dipendente> findByNome(String nome);
    
    // Restituisce tutti i dipendenti ordinati per ore settimanali di contratto crescenti
    List<Dipendente> findAllByOrderByOreSettimanaliContrattoAsc();
}
