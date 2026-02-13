package com.example.smartshift;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling; // <--- Importante

@SpringBootApplication
@EnableScheduling // <--- Aggiungi questo
public class SmartShiftApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartShiftApplication.class, args);
    }
}