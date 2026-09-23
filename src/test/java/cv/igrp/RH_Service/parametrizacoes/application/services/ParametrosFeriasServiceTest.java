package cv.igrp.RH_Service.parametrizacoes.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Sem linha em vigor no ano valem os da lei; com linha, vale a linha. */
@ExtendWith(MockitoExtension.class)
class ParametrosFeriasServiceTest {

    @Mock private ParametroFeriasRepository repository;
    @InjectMocks private ParametrosFeriasService service;

    @Test
    void semLinhaValemOsDaLei() {
        when(repository.findVigenteEm(2027)).thenReturn(Optional.empty());

        var p = service.vigenteEm(2027);

        assertTrue(p.isDaLei());
        assertEquals(LocalDate.of(2027, 3, 31), p.prazoMapa(2027));
    }

    @Test
    void comLinhaValeALinha() {
        var diplomaNovo = ParametroFerias.criar(2027, "02-15", "04-15", "05-01", "10-31", 10, "Diploma novo");
        when(repository.findVigenteEm(2027)).thenReturn(Optional.of(diplomaNovo));

        var p = service.vigenteEm(2027);

        assertFalse(p.isDaLei());
        assertEquals(LocalDate.of(2027, 4, 15), p.prazoMapa(2027));
        assertEquals(10, p.getPeriodoMinimoInterpolado());
    }
}
