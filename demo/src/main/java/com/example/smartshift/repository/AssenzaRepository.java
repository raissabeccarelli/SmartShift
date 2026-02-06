package com.example.smartshift.repository;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

public interface AssenzaRepository extends JpaRepository<Assenza, Long> {
    
    // Trova se c'è un'assenza per quel dipendente in quella data specifica
@Repository
public interface AssenzaRepository extends JpaRepository<Assenza, Long> {

    // Trova se un dipendente ha un'assenza in una data specifica
    // SELECT * FROM assenze WHERE dipendente_id = ? AND data = ?
    List<Assenza> findByDipendenteAndData(Dipendente dipendente, LocalDate data);
}