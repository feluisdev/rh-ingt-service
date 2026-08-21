package cv.igrp.RH_Service.sigdi.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import cv.igrp.RH_Service.sigdi.application.dto.KeyResultCheckinRequestDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.KeyResult;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.KeyResultRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.KeyResultId;

@ExtendWith(MockitoExtension.class)
class RegistraProcessoKrCommandHandlerTest {

    @Mock
    private KeyResultRepository keyResultRepository;

    @InjectMocks
    private RegistraProcessoKrCommandHandler handler;

    // OKR-01: proves the handler accepts a check-in without evidence -- the case that
    // was impossible while evidenceUrl carried @NotBlank in the DTO.
    @Test
    void handle_appliesCheckinWithNullEvidenceAndSaves() {
        String keyResultIdString = UUID.randomUUID().toString();
        BigDecimal valueAdded = new BigDecimal("15");
        String comment = "Progresso registado sem evidência";

        KeyResultCheckinRequestDTO dto = new KeyResultCheckinRequestDTO();
        dto.setValueAdded(valueAdded);
        dto.setComment(comment);
        dto.setEvidenceUrl(null);

        RegistraProcessoKrCommand command = new RegistraProcessoKrCommand(dto, keyResultIdString);

        KeyResult existingKeyResult = mock(KeyResult.class);

        when(keyResultRepository.findByIdFull(KeyResultId.from(keyResultIdString)))
                .thenReturn(Optional.of(existingKeyResult));
        when(existingKeyResult.applyCheckin(valueAdded, null, comment))
                .thenReturn(existingKeyResult);
        when(keyResultRepository.save(existingKeyResult)).thenReturn(existingKeyResult);

        ResponseEntity<String> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        verify(existingKeyResult).applyCheckin(valueAdded, null, comment);
        verify(keyResultRepository).save(existingKeyResult);
    }
}
