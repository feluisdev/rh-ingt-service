package cv.igrp.RH_Service.parametrizacoes.domain.service;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReferenceLookupServiceTest {

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private ReferenceLookupService referenceLookupService;

    private Option sampleOption;

    @BeforeEach
    void setUp() {
        sampleOption = Option.criar("MARITAL_STATUS", "SINGLE", "Solteiro(a)", "pt-CV", 1, null);
    }

    @Test
    void findByCcode_comLocaleExistente_deveRetornarResultadosSemFallback() {
        when(optionRepository.findByCcodeAndLocale("MARITAL_STATUS", "pt-CV", true))
            .thenReturn(List.of(sampleOption));

        var result = referenceLookupService.findByCcode("MARITAL_STATUS", "pt-CV");

        assertEquals(1, result.size());
        // apenas uma chamada — sem fallback
        verify(optionRepository, times(1)).findByCcodeAndLocale("MARITAL_STATUS", "pt-CV", true);
    }

    @Test
    void findByCcode_comLocaleInexistente_deveFazerFallbackParaPtCV() {
        when(optionRepository.findByCcodeAndLocale("MARITAL_STATUS", "en-US", true))
            .thenReturn(Collections.emptyList());
        when(optionRepository.findByCcodeAndLocale("MARITAL_STATUS", "pt-CV", true))
            .thenReturn(List.of(sampleOption));

        var result = referenceLookupService.findByCcode("MARITAL_STATUS", "en-US");

        assertEquals(1, result.size());
        verify(optionRepository).findByCcodeAndLocale("MARITAL_STATUS", "en-US", true);
        verify(optionRepository).findByCcodeAndLocale("MARITAL_STATUS", "pt-CV", true);
    }

    @Test
    void findByCcode_comLocaleNulo_deveUsarPtCV() {
        when(optionRepository.findByCcodeAndLocale("SEX", "pt-CV", true))
            .thenReturn(List.of(sampleOption));

        var result = referenceLookupService.findByCcode("SEX", null);

        assertFalse(result.isEmpty());
        verify(optionRepository, times(1)).findByCcodeAndLocale("SEX", "pt-CV", true);
    }

    @Test
    void findByCcode_comLocalePtCVSemResultados_naoDeveFazerSegundaQuery() {
        when(optionRepository.findByCcodeAndLocale("MARITAL_STATUS", "pt-CV", true))
            .thenReturn(Collections.emptyList());

        var result = referenceLookupService.findByCcode("MARITAL_STATUS", "pt-CV");

        assertTrue(result.isEmpty());
        verify(optionRepository, times(1)).findByCcodeAndLocale("MARITAL_STATUS", "pt-CV", true);
    }
}
