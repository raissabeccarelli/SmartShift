package com.example.smartshift.controller;

import com.example.smartshift.service.TurnoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/turni")
public class TurnoController {

    @Autowired
    private TurnoService turnoService;

    // Questo è il "bottone" che premeremo via browser
    @GetMapping("/genera")
    public String generaTurni() {
        // Facciamo partire l'algoritmo da Lunedì prossimo
        LocalDate inizioSettimana = LocalDate.now(); 
        
        turnoService.generaTurniPerSettimana(inizioSettimana);
        
        return "Turni generati con successo! Controlla la console e il database.";
    }
}