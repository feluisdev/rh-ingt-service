package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionRequestDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Option;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateOptionCommandHandlerTest {

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private OptionMapper optionMapper;

    @InjectMocks
    private UpdateOptionCommandHandler handler;

    private Option existingOption;
    private UUID optionUUID;

    @BeforeEach
    void setUp() {
        existingOption = Option.criar("MARITAL_STATUS", "SINGLE", "Solteiro(a)", "pt-CV", 1, null);
        optionUUID = existingOption.getId().getValor();
    }

    @Test
    void handle_comDadosValidos_deveActualizarApenasCamposEditaveis() {
        var dto = new OptionRequestDTO("MARITAL_STATUS", "SINGLE", "Solteiro(a) — novo", "pt-CV", 5, "desc");
        var command = new UpdateOptionCommand(dto, optionUUID.toString());

        when(optionRepository.findById(any())).thenReturn(Optional.of(existingOption));
        when(optionRepository.save(any())).thenReturn(existingOption);
        when(optionMapper.toDTO(any())).thenReturn(null);

        var response = handler.handle(command);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        ArgumentCaptor<Option> captor = ArgumentCaptor.forClass(Option.class);
        verify(optionRepository).save(captor.capture());
        assertEquals("MARITAL_STATUS", captor.getValue().getCcode());
        assertEquals("SINGLE", captor.getValue().getCkey());
        assertEquals("pt-CV", captor.getValue().getLocale());
        assertEquals("Solteiro(a) — novo", captor.getValue().getCvalue());
        assertEquals(5, captor.getValue().getSortOrder());
    }

    @Test
    void handle_comIdInexistente_deveLancarNotFound() {
        var dto = new OptionRequestDTO("SEX", "M", "Masculino", "pt-CV", 1, null);
        var command = new UpdateOptionCommand(dto, UUID.randomUUID().toString());

        when(optionRepository.findById(any())).thenReturn(Optional.empty());

        var ex = assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
