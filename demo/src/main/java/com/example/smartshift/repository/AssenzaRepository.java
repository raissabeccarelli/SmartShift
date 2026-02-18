package com.example.smartshift.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;

/*
 * Repository per la gestione delle operazioni CRUD su Assenza
 * Estende JpaRepository per ereditare metodi standard
*/
@Repository
public interface AssenzaRepository extends JpaRepository<Assenza, Long> {

    /*
     * Verifica se un dipendente risulta assente in una determinata data
     * Query: SELECT * FROM assenze WHERE dipendente_id = ? AND data = ?
    */
    List<Assenza> findByDipendenteAndData(Dipendente dipendente, LocalDate data);
}