package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionRequestDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateOptionCommandHandlerTest {

    @Mock
    private OptionRepository optionRepository;

    @InjectMocks
    private CreateOptionCommandHandler handler;

    @Test
    void handle_comDadosValidos_deveCriarOptionComActiveTrue() {
        var dto = new OptionRequestDTO("MARITAL_STATUS", "SINGLE", "Solteiro(a)", "pt-CV", 1, null);
        var command = new CreateOptionCommand(dto);

        var savedOption = Option.criar("MARITAL_STATUS", "SINGLE", "Solteiro(a)", "pt-CV", 1, null);
        when(optionRepository.existsByCcodeAndCkeyAndLocale("MARITAL_STATUS", "SINGLE", "pt-CV")).thenReturn(false);
        when(optionRepository.save(any())).thenReturn(savedOption);

        var response = handler.handle(command);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("optionId"));

        ArgumentCaptor<Option> captor = ArgumentCaptor.forClass(Option.class);
        verify(optionRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void handle_comCcodeInvalido_deveLancarBadRequest() {
        var dto = new OptionRequestDTO("INVALID_CODE", "KEY", "Value", "pt-CV", 1, null);
        var command = new CreateOptionCommand(dto);

        when(optionRepository.existsByCcodeAndCkeyAndLocale(any(), any(), any())).thenReturn(false);

        assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
    }

    @Test
    void handle_comEntradaDuplicada_deveLancarConflict() {
        var dto = new OptionRequestDTO("SEX", "M", "Masculino", "pt-CV", 1, null);
        var command = new CreateOptionCommand(dto);

        when(optionRepository.existsByCcodeAndCkeyAndLocale("SEX", "M", "pt-CV")).thenReturn(true);

        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void handle_comLocaleNulo_deveUsarPtCV() {
        var dto = new OptionRequestDTO("SEX", "F", "Feminino", null, null, null);
        var command = new CreateOptionCommand(dto);

        var savedOption = Option.criar("SEX", "F", "Feminino", null, null, null);
        when(optionRepository.existsByCcodeAndCkeyAndLocale("SEX", "F", "pt-CV")).thenReturn(false);
        when(optionRepository.save(any())).thenReturn(savedOption);

        var response = handler.handle(command);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(optionRepository).existsByCcodeAndCkeyAndLocale("SEX", "F", "pt-CV");
    }
}
