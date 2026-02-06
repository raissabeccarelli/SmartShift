package com.example.smartshift.repository;

import com.example.smartshift.model.Turno;
import com.example.smartshift.model.Dipendente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    // Per controllare se ha già lavorato OGGI
    List<Turno> findByDipendenteAndData(Dipendente dipendente, LocalDate data);

    // NUOVO: Dammi tutti i turni di questo dipendente in questo range di date
    // (Il calcolo delle ore lo faremo noi in Java)
    List<Turno> findByDipendenteAndDataBetween(Dipendente dipendente, LocalDate inizio, LocalDate fine);

    List<Turno> findByDipendenteAndDataBetweenOrderByDataDescOraFineDesc(
        Dipendente dipendente, LocalDate inizio, LocalDate fine
    );
}