package com.example.smartshift.repository;

import com.example.smartshift.model.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DipendenteRepository extends JpaRepository<Dipendente, Long> {

    // Trova i dipendenti per nome
    List<Dipendente> findByNome(String nome);
    
    // Ordina per ore contratto (crescenti) - NOTA LE PARENTESI <>
    List<Dipendente> findAllByOrderByOreSettimanaliContrattoAsc();
}
