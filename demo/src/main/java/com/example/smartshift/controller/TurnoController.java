package com.example.smartshift.controller;

import com.example.smartshift.model.Turno;
import com.example.smartshift.service.TurnoService;
import com.example.smartshift.repository.TurnoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/turni")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    @Autowired
    private TurnoRepository turnoRepository; // <--- Ci serve per leggere i dati

    // 1. Endpoint per GENERARE i turni (già fatto)
    @GetMapping("/genera")
    public String generaTurni() {
        // Qui decidiamo: "Genera turni partendo da OGGI"
        turnoService.generaTurniPerSettimana(LocalDate.now()); 
        
        return "Turni generati con successo!";
    }

    // 2. Endpoint per LEGGERE i turni (vai su http://localhost:8080/api/turni per vedere i dati grezzi)
    @GetMapping
    public List<Turno> getTuttiTurni() {
        return turnoRepository.findAll();
    }
}