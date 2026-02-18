package com.example.smartshift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <--- Importante

/*
 * Classe principale dell'applicazione che avvia
 * il contesto Spring Boot e configura i componenti del sistema
 */
@SpringBootApplication // Abilita auto-configurazione, scansione dei componenti e configurazione Spring
@EnableScheduling // Attiva il supporto per task schedulati (@Scheduled)
public class SmartShiftApplication {
    public static void main(String[] args) {
        // Avvia l'applicazione e inizializza il contesto Spring
        SpringApplication.run(SmartShiftApplication.class, args);
    }
}