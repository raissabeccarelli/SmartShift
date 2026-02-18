package com.example.smartshift.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.smartshift.model.Dipendente;
import com.example.smartshift.model.Turno;
import com.example.smartshift.repository.AssenzaRepository;
import com.example.smartshift.repository.DipendenteRepository;
import com.example.smartshift.repository.TurnoRepository;
import com.example.smartshift.service.TurnoService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = TurnoController.class, 
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    })
@AutoConfigureMockMvc(addFilters = false)
class TurnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TurnoService turnoService;

    @MockBean
    private TurnoRepository turnoRepository;

    @MockBean
    private DipendenteRepository dipendenteRepository;

    @MockBean
    private AssenzaRepository assenzaRepository;

    @Test
    @DisplayName("GET /config-slot")
    void testGetConfigurazioneSlot() throws Exception {
        when(turnoService.getConfigurazioneSlot()).thenReturn(List.of(Map.of("inizio", "08:00")));
        mockMvc.perform(get("/api/turni/config-slot"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /genera - Successo")
    void testGeneraTurni_Success() throws Exception {
        TurnoController.GenerazioneRequest req = new TurnoController.GenerazioneRequest();
        req.setData("2023-10-23");
        req.setMinMattina(2);
        req.setMinPomeriggio(2);
        req.setMinSera(2);
        req.setMinNotte(1);

        mockMvc.perform(post("/api/turni/genera")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /manuale - Aggiunta turno manuale")
    void testAggiungiTurnoManuale() throws Exception {
        TurnoController.TurnoManualeRequest req = new TurnoController.TurnoManualeRequest();
        req.dipendenteId = 1L;
        req.data = "2023-10-25";
        req.oraInizio = "08:00";
        req.oraFine = "12:00";
        
        Dipendente d = new Dipendente();
        d.setId(1L);

        when(dipendenteRepository.findById(1L)).thenReturn(Optional.of(d));
        when(turnoRepository.save(any(Turno.class))).thenReturn(new Turno());

        mockMvc.perform(post("/api/turni/manuale")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Turno aggiunto"));
    }

    @Test
    @DisplayName("GET / (Tutti i turni)")
    void testGetTuttiTurni() throws Exception {
        when(turnoRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/turni"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /{id} - Elimina turno")
    void testEliminaTurno() throws Exception {
        when(turnoRepository.existsById(1L)).thenReturn(true);
        doNothing().when(turnoRepository).deleteById(1L);

        mockMvc.perform(delete("/api/turni/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Turno eliminato"));
    }

    @Test
    @DisplayName("POST /assenza - Inserimento valido")
    void testInserisciAssenza_Success() throws Exception {
        TurnoController.AssenzaRequest req = new TurnoController.AssenzaRequest();
        req.dipendenteId = 1L;
        req.dataInizio = "2023-10-20";
        req.dataFine = "2023-10-21";
        req.tipo = "FERIE";
        req.motivazione = "Vacanza";
        
        Dipendente d = new Dipendente();
        when(dipendenteRepository.findById(1L)).thenReturn(Optional.of(d));
        when(assenzaRepository.findByDipendenteAndData(any(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/api/turni/assenza")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /assenza - Errore date (Fine prima di Inizio)")
    void testInserisciAssenza_DateErrate() throws Exception {
        TurnoController.AssenzaRequest req = new TurnoController.AssenzaRequest();
        req.dataInizio = "2023-10-25";
        req.dataFine = "2023-10-20"; // Errore!

        mockMvc.perform(post("/api/turni/assenza")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La data di fine non può essere precedente all'inizio"));
    }

    @Test
    @DisplayName("GET /assenze - Lista completa")
    void testGetTutteAssenze() throws Exception {
        when(assenzaRepository.findAll()).thenReturn(Collections.emptyList());
        mockMvc.perform(get("/api/turni/assenze"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /assenza/{id} - Successo")
    void testEliminaAssenza() throws Exception {
        when(assenzaRepository.existsById(1L)).thenReturn(true);
        
        mockMvc.perform(delete("/api/turni/assenza/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Assenza eliminata con successo"));
    }

    @Test
    @DisplayName("DELETE /assenza/{id} - Non Trovato")
    void testEliminaAssenza_NotFound() throws Exception {
        when(assenzaRepository.existsById(1L)).thenReturn(false);
        
        mockMvc.perform(delete("/api/turni/assenza/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Assenza non trovata"));
    }

    @Test
    @DisplayName("POST /dipendenti - Aggiungi")
    void testAggiungiDipendente() throws Exception {
        Dipendente d = new Dipendente("A", "B", 40, 8, 26);
        when(dipendenteRepository.save(any(Dipendente.class))).thenReturn(d);

        mockMvc.perform(post("/api/turni/dipendenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(d)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /dipendenti")
    void testGetDipendenti() throws Exception {
        mockMvc.perform(get("/api/turni/dipendenti"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /dipendenti/{id}")
    void testEliminaDipendente() throws Exception {
        Dipendente d = new Dipendente();
        when(dipendenteRepository.findById(1L)).thenReturn(Optional.of(d));
        
        mockMvc.perform(delete("/api/turni/dipendenti/{id}", 1L))
                .andExpect(status().isOk());
    }
}