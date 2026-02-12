package com.example.smartshift.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    List<Turno> findByDipendenteAndData(Dipendente dipendente, LocalDate data);
    
    List<Turno> findByDipendenteAndDataBetween(Dipendente dipendente, LocalDate start, LocalDate end);
    
    List<Turno> findByDipendenteAndDataBetweenOrderByDataDescOraFineDesc(Dipendente dipendente, LocalDate start, LocalDate end);

    // --- CORREZIONE QUI ---
    @Modifying // Dice a Spring: "Questa query modifica/cancella dati"
    @Transactional // Dice a Spring: "Gestisci questa operazione come una transazione sicura"
    void deleteByDataBetween(LocalDate dataInizio, LocalDate dataFine);

    @Modifying
    @Query("DELETE FROM Turno t WHERE t.data >= :dataInizio AND t.data <= :dataFine")
    void deleteTurniInRange(@Param("dataInizio") LocalDate dataInizio, @Param("dataFine") LocalDate dataFine);

    void deleteByDipendente(Dipendente dipendente);
}