package com.example.smartshift.repository;

import com.example.smartshift.model.Assenza;
import com.example.smartshift.model.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AssenzaRepository extends JpaRepository<Assenza, Long> {
    
    // Trova se c'è un'assenza per quel dipendente in quella data specifica
    List<Assenza> findByDipendenteAndData(Dipendente dipendente, LocalDate data);
}