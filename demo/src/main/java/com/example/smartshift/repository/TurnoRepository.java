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

/*
 * Repository per la gestione delle operazioni CRUD su Turno
 * Estende JpaRepository per ereditare metodi standard
*/
@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {
    
    // Recupera i turni di un dipendente in una data specifica
    List<Turno> findByDipendenteAndData(Dipendente dipendente, LocalDate data);
    
    // Recupera i turni di un dipendente in un intervallo di date
    List<Turno> findByDipendenteAndDataBetween(Dipendente dipendente, LocalDate start, LocalDate end);
    
    // Recupera i turni in un intervallo ordinati per data decrescente
    // ed ora di fine decrescente (per individuare l'ultimo turno svolto)
    List<Turno> findByDipendenteAndDataBetweenOrderByDataDescOraFineDesc(Dipendente dipendente, LocalDate start, LocalDate end);

    // Elimina tutti i turni compresi tra le due date indicate
    @Modifying 
    @Transactional 
    void deleteByDataBetween(LocalDate dataInizio, LocalDate dataFine);

    @Modifying
    @Query("DELETE FROM Turno t WHERE t.data >= :dataInizio AND t.data <= :dataFine")
    void deleteTurniInRange(@Param("dataInizio") LocalDate dataInizio, @Param("dataFine") LocalDate dataFine);

    // Elimina tutti i turni associati a un determinato dipendente
    // Utilizzato prima della rimozione del dipendente, mantiene coerenza referenziale
    void deleteByDipendente(Dipendente dipendente);
}