package com.example.smartshift.repository;

import com.example.smartshift.model.Turno;
import com.example.smartshift.model.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Trova tutti i turni di un dipendente in una data specifica
    List<Turno> findByDipendenteAndData(Dipendente dipendente, LocalDate data);

    // Query per verificare il riposo di 11 ore.
    // Cerca l'ultimo turno del giorno precedente per vedere a che ora ha finito.
    Turno findFirstByDipendenteAndDataOrderByOraFineDesc(Dipendente dipendente, LocalDate data);
}