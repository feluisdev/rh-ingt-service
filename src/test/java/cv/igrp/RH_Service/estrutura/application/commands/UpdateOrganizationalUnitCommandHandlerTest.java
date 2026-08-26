package cv.igrp.RH_Service.estrutura.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitRequestDTO;
import cv.igrp.RH_Service.estrutura.application.dto.OrganizationalUnitResponseDTO;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupDTO;
import cv.igrp.RH_Service.estrutura.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.OrganizationalUnitMapper;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

// Primeiro conjunto de testes do modulo estrutura -- src/test/.../estrutura/ estava
// vazio antes deste plano (109-02-PLAN.md, Task 3).
@ExtendWith(MockitoExtension.class)
public class UpdateOrganizationalUnitCommandHandlerTest {

    @Mock
    private OrganizationalUnitRepository unitRepository;

    @Mock
    private OrganizationalUnitMapper mapper;

    @Mock
    private OptionLookupPort optionLookupPort;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    @InjectMocks
    private UpdateOrganizationalUnitCommandHandler handler;

    private static OrganizationalUnit existingUnit(OrganizationalUnitId id, UUID responsibleEmployeeId) {
        return OrganizationalUnit.reconstruir(id, "U1", "Unidade 1", "U1",
                "DIRECAO", "desc", null, responsibleEmployeeId, true);
    }

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
        UUID unitIdRaw = UUID.randomUUID();
        OrganizationalUnitId unitId = OrganizationalUnitId.from(unitIdRaw);
        UUID responsibleId = UUID.randomUUID();

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId, null)));
        when(unitRepository.existsByCodeAndIdNot("U1", unitId)).thenReturn(false);
        when(funcionarioLookupPort.findById(responsibleId))
                .thenReturn(Optional.of(new FuncionarioLookupDTO(responsibleId, "Fulano Tal")));
        when(unitRepository.save(any(OrganizationalUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(OrganizationalUnit.class))).thenReturn(new OrganizationalUnitResponseDTO());

        var command = new UpdateOrganizationalUnitCommand(unitIdRaw.toString(), requestDto(responsibleId));
        handler.handle(command);

        // Captura o argumento gravado -- afirmar sobre o valor devolvido nao
        // distinguiria "gravou" de "devolveu o que lhe deram" (109-02-PLAN.md).
        ArgumentCaptor<OrganizationalUnit> captor = ArgumentCaptor.forClass(OrganizationalUnit.class);
        verify(unitRepository).save(captor.capture());
        assertEquals(responsibleId, captor.getValue().getResponsibleEmployeeId());
    }

    @Test
    void handleWithUnknownResponsibleEmployeeThrowsBadRequestAndNeverSaves() {
        UUID unitIdRaw = UUID.randomUUID();
        OrganizationalUnitId unitId = OrganizationalUnitId.from(unitIdRaw);
        UUID responsibleId = UUID.randomUUID();

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId, null)));
        when(unitRepository.existsByCodeAndIdNot("U1", unitId)).thenReturn(false);
        when(funcionarioLookupPort.findById(responsibleId)).thenReturn(Optional.empty());

        var command = new UpdateOrganizationalUnitCommand(unitIdRaw.toString(), requestDto(responsibleId));

        IgrpResponseStatusException exception =
                assertThrows(IgrpResponseStatusException.class, () -> handler.handle(command));

        assertEquals(400, exception.getBody().getStatus());
        verify(unitRepository, never()).save(any());
    }

    @Test
    void handleWithNullResponsibleEmployeeSavesNullWithoutConsultingPort() {
        UUID unitIdRaw = UUID.randomUUID();
        OrganizationalUnitId unitId = OrganizationalUnitId.from(unitIdRaw);

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId, null)));
        when(unitRepository.existsByCodeAndIdNot("U1", unitId)).thenReturn(false);
        when(unitRepository.save(any(OrganizationalUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(OrganizationalUnit.class))).thenReturn(new OrganizationalUnitResponseDTO());

        var command = new UpdateOrganizationalUnitCommand(unitIdRaw.toString(), requestDto(null));
        handler.handle(command);

        ArgumentCaptor<OrganizationalUnit> captor = ArgumentCaptor.forClass(OrganizationalUnit.class);
        verify(unitRepository).save(captor.capture());
        assertNull(captor.getValue().getResponsibleEmployeeId());
        // D-05: ausencia de responsavel e estado legitimo -- a porta nem e consultada.
        verifyNoInteractions(funcionarioLookupPort);
    }

    // O gume de D-10, pinado (109-02-PLAN.md): PUT e substituicao total, mesma
    // semantica de parentUnitId. Uma unidade que ja tem responsavel, sujeita a um
    // pedido que omite o campo, fica SEM responsavel depois deste pedido. Este caso
    // nao descreve um bug -- documenta a semantica, e falha se alguem a mudar sem
    // querer.
    @Test
    void handleWithOmittedResponsibleEmployeeClearsExistingValueByTotalReplacementSemantics() {
        UUID unitIdRaw = UUID.randomUUID();
        OrganizationalUnitId unitId = OrganizationalUnitId.from(unitIdRaw);
        UUID previousResponsible = UUID.randomUUID();

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId, previousResponsible)));
        when(unitRepository.existsByCodeAndIdNot("U1", unitId)).thenReturn(false);
        when(unitRepository.save(any(OrganizationalUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(OrganizationalUnit.class))).thenReturn(new OrganizationalUnitResponseDTO());

        var command = new UpdateOrganizationalUnitCommand(unitIdRaw.toString(), requestDto(null));
        handler.handle(command);

        ArgumentCaptor<OrganizationalUnit> captor = ArgumentCaptor.forClass(OrganizationalUnit.class);
        verify(unitRepository).save(captor.capture());
        assertNull(captor.getValue().getResponsibleEmployeeId());
    }

    @Test
    void handleResolvesResponsibleEmployeeNameInResponse() {
        UUID unitIdRaw = UUID.randomUUID();
        OrganizationalUnitId unitId = OrganizationalUnitId.from(unitIdRaw);
        UUID responsibleId = UUID.randomUUID();

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(existingUnit(unitId, null)));
        when(unitRepository.existsByCodeAndIdNot("U1", unitId)).thenReturn(false);
        when(funcionarioLookupPort.findById(responsibleId))
                .thenReturn(Optional.of(new FuncionarioLookupDTO(responsibleId, "Fulano Tal")));
        when(unitRepository.save(any(OrganizationalUnit.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toDTO(any(OrganizationalUnit.class))).thenReturn(new OrganizationalUnitResponseDTO());

        var command = new UpdateOrganizationalUnitCommand(unitIdRaw.toString(), requestDto(responsibleId));
        ResponseEntity<OrganizationalUnitResponseDTO> response = handler.handle(command);

        assertNotNull(response.getBody());
        assertEquals("Fulano Tal", response.getBody().getResponsibleEmployeeName());
    }
}
