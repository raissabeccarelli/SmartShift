package com.example.smartshift.service;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.smartshift.model.Dipendente;

public interface ShiftRule {
    boolean isValid(Dipendente dipendente, LocalDate data, LocalTime inizio, LocalTime fine);
}