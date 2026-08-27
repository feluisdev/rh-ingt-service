package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EligibilitySkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsibleDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.port.FuncionarioLookupPort;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.PaaSubmissionPeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova do critério 3 da Fase 116: <b>quem tem responsabilidade não é automaticamente quem
 * tem de preencher</b>.
 * <p>
 * <b>O que este teste prova.</b> A lista de elegíveis decide-se pelo cadastro de
 * enquadramento (ramo {@code INDIVIDUAL_LEVEL}) e pelo {@code responsibleEmployeeId} da
 * unidade (ramo {@code UNIT_LEVEL}) -- duas fontes distintas. Ser o responsável de uma
 * unidade não põe ninguém na lista individual dessa unidade nesse ano: o funcionário R é o
 * {@code responsibleEmployeeId} da unidade U, mas não tem enquadramento em U a cobrir 2027;
 * o funcionário E tem. Em {@code INDIVIDUAL_LEVEL}/2027, o grupo de U contém E e não contém
 * R. Na mesma unidade, no mesmo ano, com os mesmos dados, o ramo {@code UNIT_LEVEL} devolve
 * R -- a diferença é a fonte consultada, não os dados.
 * <p>
 * <b>O que este teste NÃO prova.</b> Não prova nada contra a stack de autorização IGRP da
 * Fase 115, porque essa stack não existe neste ramo: {@code v26.0/prazos} foi ramificado de
 * {@code 9510351}, antes da dependência {@code cv.igrp.framework.auth:core-spring-boot},
 * cujo bytecode Java 26 impede o arranque em JDK 23 (ver 116-CONTEXT.md, nota de ramo). A
 * confrontação directa entre uma permissão IGRP concedida e uma elegibilidade ausente só é
 * possível depois de os dois ramos serem fundidos -- e essa fusão é trabalho do operador,
 * não desta fase. O {@code responsibleEmployeeId} é aqui usado como o representante mais
 * forte de "quem pode agir sobre a unidade" que existe neste ramo, e é escolhido por essa
 * razão, e não por conveniência: não há, em {@code v26.0/prazos}, um conceito de permissão
 * concedida com o qual confrontar directamente a elegibilidade.
 */
@ExtendWith(MockitoExtension.class)
class EligibilityIsNotPermissionTest {

    private static final int YEAR_WITH_COVERAGE = 2027;
    private static final int YEAR_WITHOUT_COVERAGE = 2026;

    @Mock
    private OrganicaLookupPort organicaLookupPort;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    private EligibleResponsiblesResolver resolver;

    private final UUID unitId = UUID.randomUUID();
    private final UUID responsibleId = UUID.randomUUID(); // R -- "quem pode" (responsável da unidade)
    private final UUID assignedEmployeeId = UUID.randomUUID(); // E -- enquadrado em U a cobrir 2027

    private OrganicaDTO unitU() {
        OrganicaDTO dto = new OrganicaDTO();
        dto.setId(unitId.toString());
        dto.setName("Unidade U");
        dto.setAcronym("U");
        dto.setResponsibleEmployeeId(responsibleId.toString());
        return dto;
    }

    private PaaSubmissionPeriod periodOf(PaaLevel type, int year) {
        return PaaSubmissionPeriod.reconstruct(UUID.randomUUID(), Purpose.PAA, type,
                LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31), "OPEN", year);
    }

    private void setUpCommonStubs() {
        resolver = new EligibleResponsiblesResolver(organicaLookupPort, funcionarioLookupPort);
        lenient().when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unitU()));
        // R existe como funcionário -- é resolúvel por findById, o que UNIT_LEVEL usa.
        FuncionarioDTO responsibleEmployee = new FuncionarioDTO();
        responsibleEmployee.setId(responsibleId.toString());
        responsibleEmployee.setNomeCompleto("R (responsável da unidade)");
        lenient().when(funcionarioLookupPort.findById(responsibleId)).thenReturn(Optional.of(responsibleEmployee));
        // R NÃO tem enquadramento em U a cobrir 2027 -- só E tem.
        lenient().when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(eq(unitId), eq(YEAR_WITH_COVERAGE)))
                .thenReturn(List.of(assignedEmployeeId));
        FuncionarioDTO assignedEmployee = new FuncionarioDTO();
        assignedEmployee.setId(assignedEmployeeId.toString());
        assignedEmployee.setNomeCompleto("E (enquadrado em U em 2027)");
        lenient().when(funcionarioLookupPort.findAllByIds(List.of(assignedEmployeeId)))
                .thenReturn(Map.of(assignedEmployeeId, assignedEmployee));
        // Em 2026 nem R nem E têm enquadramento em U.
        lenient().when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(eq(unitId), eq(YEAR_WITHOUT_COVERAGE)))
                .thenReturn(List.of());
    }

    @Test
    void individualLevel2027_groupContainsEAndDoesNotContainR() {
        setUpCommonStubs();

        EligibleResponsiblesDTO result = resolver.resolve(periodOf(PaaLevel.INDIVIDUAL_LEVEL, YEAR_WITH_COVERAGE));

        assertEquals(1, result.getGroups().size());
        List<String> employeeIds = result.getGroups().get(0).getResponsibles().stream()
                .map(EligibleResponsibleDTO::getEmployeeId)
                .toList();
        assertTrue(employeeIds.contains(assignedEmployeeId.toString()),
                "E, que tem enquadramento em U a cobrir 2027, tem de estar na lista individual");
        assertFalse(employeeIds.contains(responsibleId.toString()),
                "R, que é responsável de U mas não tem enquadramento em U a cobrir 2027, NÃO pode estar na lista individual -- ter responsabilidade não é ter elegibilidade");
    }

    @Test
    void unitLevel2027_groupContainsR_sameUnitSameYearDifferentSource() {
        setUpCommonStubs();

        EligibleResponsiblesDTO result = resolver.resolve(periodOf(PaaLevel.UNIT_LEVEL, YEAR_WITH_COVERAGE));

        assertEquals(1, result.getGroups().size());
        List<String> employeeIds = result.getGroups().get(0).getResponsibles().stream()
                .map(EligibleResponsibleDTO::getEmployeeId)
                .toList();
        assertTrue(employeeIds.contains(responsibleId.toString()),
                "R é o responsibleEmployeeId de U -- no ramo UNIT_LEVEL é R quem aparece, mesma unidade e mesmo ano que no teste anterior, onde não aparecia");
    }

    @Test
    void individualLevel2026_unitIsSkipped_rStillAbsentDespiteBeingResponsible() {
        setUpCommonStubs();

        EligibleResponsiblesDTO result = resolver.resolve(periodOf(PaaLevel.INDIVIDUAL_LEVEL, YEAR_WITHOUT_COVERAGE));

        assertTrue(result.getGroups().isEmpty(),
                "Nem R nem E têm enquadramento em U a cobrir 2026 -- a unidade não pode gerar grupo nenhum");
        assertEquals(1, result.getSkipped().size());
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_ASSIGNED_EMPLOYEES.getCode(), result.getSkipped().get(0).getReason());
        // R continua fora da lista mesmo sendo, em qualquer leitura informal, "quem pode
        // agir sobre U" -- ser responsável não compensa a ausência de enquadramento.
    }
}
