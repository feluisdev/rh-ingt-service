package cv.igrp.RH_Service.estrutura.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitRequestDTO;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupDTO;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CreateOrganizationalUnitCommandHandlerTest {

    @Mock
    private OrganizationalUnitRepository unitRepository;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    @InjectMocks
    private CreateOrganizationalUnitCommandHandler handler;

    private static OrganizationalUnitRequestDTO requestDto(UUID responsibleEmployeeId) {
        OrganizationalUnitRequestDTO dto = new OrganizationalUnitRequestDTO();
        dto.setCode("U1");
        dto.setName("Unidade 1");
        dto.setAcronym("U1");
        dto.setUnitType("DIRECAO");
        dto.setDescricao("desc");
        dto.setResponsibleEmployeeId(responsibleEmployeeId);
        return dto;
    }

    @Test
    void handleWithValidResponsibleEmployeeSavesId() {
        UUID responsibleId = UUID.randomUUID();

        when(unitRepository.existsByCode("U1")).thenReturn(false);
        when(funcionarioLookupPort.findById(responsibleId))
                .thenReturn(Optional.of(new FuncionarioLookupDTO(responsibleId, "Fulano Tal")));
        when(unitRepository.save(any(OrganizationalUnit.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateOrganizationalUnitCommand(requestDto(responsibleId));
        handler.handle(command);

        ArgumentCaptor<OrganizationalUnit> captor = ArgumentCaptor.forClass(OrganizationalUnit.class);
        verify(unitRepository).save(captor.capture());
        assertEquals(responsibleId, captor.getValue().getResponsibleEmployeeId());
    }

    @Test
    void handleWithUnknownResponsibleEmployeeThrowsBadRequestAndNeverSaves() {
        UUID responsibleId = UUID.randomUUID();

        when(unitRepository.existsByCode("U1")).thenReturn(false);
        when(funcionarioLookupPort.findById(responsibleId)).thenReturn(Optional.empty());

        var command = new CreateOrganizationalUnitCommand(requestDto(responsibleId));

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(unitRepository, never()).save(any());
    }

    @Test
    void handleWithNullResponsibleEmployeeSavesNullWithoutConsultingPort() {
        when(unitRepository.existsByCode("U1")).thenReturn(false);
        when(unitRepository.save(any(OrganizationalUnit.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new CreateOrganizationalUnitCommand(requestDto(null));
        handler.handle(command);

        ArgumentCaptor<OrganizationalUnit> captor = ArgumentCaptor.forClass(OrganizationalUnit.class);
        verify(unitRepository).save(captor.capture());
        assertNull(captor.getValue().getResponsibleEmployeeId());
        verifyNoInteractions(funcionarioLookupPort);
    }
}
