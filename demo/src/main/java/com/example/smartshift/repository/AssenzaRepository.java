package com.example.smartshift.repository;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AssenzaRepository extends JpaRepository<Assenza, Long> {

    // Trova se un dipendente ha un'assenza in una data specifica
    // SELECT * FROM assenze WHERE dipendente_id = ? AND data = ?
    List<Assenza> findByDipendenteAndData(Dipendente dipendente, LocalDate data);
}