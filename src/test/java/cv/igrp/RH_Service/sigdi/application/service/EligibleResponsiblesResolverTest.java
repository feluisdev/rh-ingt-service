package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EligibilitySkipReason;
import cv.igrp.RH_Service.sigdi.application.constants.PaaLevel;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleResponsiblesDTO;
import cv.igrp.RH_Service.sigdi.application.dto.EligibleUnitGroupDTO;
import cv.igrp.RH_Service.sigdi.application.dto.FuncionarioDTO;
import cv.igrp.RH_Service.sigdi.application.dto.OrganicaDTO;
import cv.igrp.RH_Service.sigdi.application.dto.SkippedUnitDTO;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cobre o comportamento de {@link EligibleResponsiblesResolver} descrito no bloco
 * {@code behavior} de 116-03-PLAN.md. Usa 2027 como ano fixo do período em todos os
 * cenários INDIVIDUAL_LEVEL, distinto do ano corrente, precisamente para exercitar
 * "o ano usado é o do período e não o do relógio". Os identificadores de unidade usados
 * são UUIDs reais -- o ramo INDIVIDUAL_LEVEL faz {@code UUID.fromString(unit.getId())}
 * para consultar o enquadramento, pelo que um id de teste não-UUID rebentaria em runtime
 * mesmo com um matcher {@code any()} do lado do mock.
 */
@ExtendWith(MockitoExtension.class)
class EligibleResponsiblesResolverTest {

    private static final int PERIOD_YEAR = 2027;

    @Mock
    private OrganicaLookupPort organicaLookupPort;

    @Mock
    private FuncionarioLookupPort funcionarioLookupPort;

    @InjectMocks
    private EligibleResponsiblesResolver resolver;

    private OrganicaDTO unit(UUID id, String name, String acronym, String responsibleEmployeeId) {
        OrganicaDTO dto = new OrganicaDTO();
        dto.setId(id.toString());
        dto.setName(name);
        dto.setAcronym(acronym);
        dto.setResponsibleEmployeeId(responsibleEmployeeId);
        return dto;
    }

    private FuncionarioDTO funcionario(String id, String nomeCompleto) {
        FuncionarioDTO dto = new FuncionarioDTO();
        dto.setId(id);
        dto.setNomeCompleto(nomeCompleto);
        return dto;
    }

    private PaaSubmissionPeriod period(PaaLevel type) {
        return PaaSubmissionPeriod.reconstruct(UUID.randomUUID(), Purpose.PAA, type,
                LocalDate.of(PERIOD_YEAR, 1, 1), LocalDate.of(PERIOD_YEAR, 12, 31), "OPEN", PERIOD_YEAR);
    }

    // --- UNIT_LEVEL ---

    @Test
    void unitLevel_unitWithResponsibleAndEmployeeExists_returnsGroupWithOneResponsible() {
        UUID unitId = UUID.randomUUID();
        UUID responsibleId = UUID.randomUUID();
        OrganicaDTO unit = unit(unitId, "Direção Financeira", "DF", responsibleId.toString());
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findById(responsibleId))
                .thenReturn(Optional.of(funcionario(responsibleId.toString(), "Ana Silva")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertEquals(1, result.getGroups().size());
        assertTrue(result.getSkipped().isEmpty());
        EligibleUnitGroupDTO group = result.getGroups().get(0);
        assertEquals(unitId.toString(), group.getUnitId());
        assertEquals(1, group.getResponsibles().size());
        assertEquals(responsibleId.toString(), group.getResponsibles().get(0).getEmployeeId());
        assertEquals("Ana Silva", group.getResponsibles().get(0).getEmployeeName());
    }

    @Test
    void unitLevel_unitWithNullResponsible_isSkippedWithUnitWithoutResponsible() {
        UUID unitId = UUID.randomUUID();
        OrganicaDTO unit = unit(unitId, "Direção Jurídica", "DJ", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertTrue(result.getGroups().isEmpty());
        assertEquals(1, result.getSkipped().size());
        SkippedUnitDTO skip = result.getSkipped().get(0);
        assertEquals(unitId.toString(), skip.getUnitId());
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE.getCode(), skip.getReason());
    }

    @Test
    void unitLevel_unitWithBlankOrUnparseableResponsible_isSkippedWithRawValueInDetail() {
        OrganicaDTO unitBlank = unit(UUID.randomUUID(), "Direção Blank", "DB", "   ");
        OrganicaDTO unitBad = unit(UUID.randomUUID(), "Direção Ruído", "DR", "nao-e-um-uuid");
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unitBlank, unitBad));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertTrue(result.getGroups().isEmpty());
        assertEquals(2, result.getSkipped().size());
        SkippedUnitDTO skipBlank = result.getSkipped().get(0);
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE.getCode(), skipBlank.getReason());
        assertEquals("   ", skipBlank.getDetail());
        SkippedUnitDTO skipBad = result.getSkipped().get(1);
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_RESPONSIBLE.getCode(), skipBad.getReason());
        assertEquals("nao-e-um-uuid", skipBad.getDetail());
    }

    @Test
    void unitLevel_unitWithOrphanResponsible_isSkippedWithResponsibleNotFound() {
        UUID orphanId = UUID.randomUUID();
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção Órfã", "DO", orphanId.toString());
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findById(orphanId)).thenReturn(Optional.empty());

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertTrue(result.getGroups().isEmpty());
        assertEquals(1, result.getSkipped().size());
        SkippedUnitDTO skip = result.getSkipped().get(0);
        assertEquals(EligibilitySkipReason.RESPONSIBLE_NOT_FOUND.getCode(), skip.getReason());
        assertEquals(orphanId.toString(), skip.getDetail());
    }

    @Test
    void unitLevel_oneSkippedUnitDoesNotRemoveOthers_twoGroupsOneSkip() {
        UUID r1 = UUID.randomUUID();
        UUID r2 = UUID.randomUUID();
        OrganicaDTO good1 = unit(UUID.randomUUID(), "Direção A", "DA", r1.toString());
        OrganicaDTO good2 = unit(UUID.randomUUID(), "Direção B", "DB", r2.toString());
        OrganicaDTO bad = unit(UUID.randomUUID(), "Direção C", "DC", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(good1, good2, bad));
        when(funcionarioLookupPort.findById(r1)).thenReturn(Optional.of(funcionario(r1.toString(), "Bruno")));
        when(funcionarioLookupPort.findById(r2)).thenReturn(Optional.of(funcionario(r2.toString(), "Carla")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertEquals(2, result.getGroups().size());
        assertEquals(1, result.getSkipped().size());
        assertEquals(2, result.getTotalEligible());
    }

    @Test
    void unitLevel_neverConsultsParentUnitId() {
        // Nenhum stub de parentUnitId é fornecido; a garantia estrutural de que este ramo
        // não consulta parentUnitId fica também reforçada pela verificação automatizada
        // por grep sobre o próprio ficheiro do resolvedor (task 2 do plano).
        UUID r1 = UUID.randomUUID();
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção D", "DD", r1.toString());
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findById(r1)).thenReturn(Optional.of(funcionario(r1.toString(), "Duarte")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertEquals(1, result.getGroups().size());
    }

    // --- INDIVIDUAL_LEVEL ---

    @Test
    void individualLevel_unitWithThreeAssignedEmployees_returnsGroupWithThreeResponsibles() {
        UUID e1 = UUID.randomUUID();
        UUID e2 = UUID.randomUUID();
        UUID e3 = UUID.randomUUID();
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção E", "DE", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(any(), eq(PERIOD_YEAR)))
                .thenReturn(List.of(e1, e2, e3));
        when(funcionarioLookupPort.findAllByIds(any())).thenReturn(Map.of(
                e1, funcionario(e1.toString(), "Eva"),
                e2, funcionario(e2.toString(), "Filipe"),
                e3, funcionario(e3.toString(), "Gil")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.INDIVIDUAL_LEVEL));

        assertEquals(1, result.getGroups().size());
        assertEquals(3, result.getGroups().get(0).getResponsibles().size());
        assertEquals(3, result.getTotalEligible());
    }

    @Test
    void individualLevel_usesPeriodYearNotClockYear() {
        UUID e1 = UUID.randomUUID();
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção F", "DF2", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(any(), eq(PERIOD_YEAR)))
                .thenReturn(List.of(e1));
        when(funcionarioLookupPort.findAllByIds(any())).thenReturn(Map.of(e1, funcionario(e1.toString(), "Hugo")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.INDIVIDUAL_LEVEL));

        // A stub só responde para PERIOD_YEAR (2027, fixo e distinto do ano corrente do
        // relógio). Se o resolvedor usasse LocalDate.now().getYear() em vez de
        // period.getYear(), o Mockito devolveria a lista vazia por omissão e o grupo
        // ficaria vazio -- a asserção de tamanho abaixo é o que exercita a distinção.
        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getGroups().get(0).getResponsibles().size());
    }

    @Test
    void individualLevel_unitWithNoAssignedEmployees_isSkippedWithUnitWithoutAssignedEmployees() {
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção G", "DG", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(any(), eq(PERIOD_YEAR)))
                .thenReturn(List.of());

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.INDIVIDUAL_LEVEL));

        assertTrue(result.getGroups().isEmpty());
        assertEquals(1, result.getSkipped().size());
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_ASSIGNED_EMPLOYEES.getCode(), result.getSkipped().get(0).getReason());
    }

    @Test
    void individualLevel_orphanEmployeeIdPartiallyExcluded_groupSurvivesWithoutOrphan() {
        UUID resolved = UUID.randomUUID();
        UUID orphan = UUID.randomUUID();
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção H", "DH", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(any(), eq(PERIOD_YEAR)))
                .thenReturn(List.of(resolved, orphan));
        when(funcionarioLookupPort.findAllByIds(any()))
                .thenReturn(Map.of(resolved, funcionario(resolved.toString(), "Ivo")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.INDIVIDUAL_LEVEL));

        assertEquals(1, result.getGroups().size());
        assertEquals(1, result.getGroups().get(0).getResponsibles().size());
        assertEquals(resolved.toString(), result.getGroups().get(0).getResponsibles().get(0).getEmployeeId());
        assertTrue(result.getSkipped().isEmpty());
    }

    @Test
    void individualLevel_allEmployeeIdsOrphan_unitIsSkippedWithOrphanIdsInDetail() {
        UUID orphan1 = UUID.randomUUID();
        UUID orphan2 = UUID.randomUUID();
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção I", "DI", null);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(any(), eq(PERIOD_YEAR)))
                .thenReturn(List.of(orphan1, orphan2));
        when(funcionarioLookupPort.findAllByIds(any())).thenReturn(Map.of());

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.INDIVIDUAL_LEVEL));

        assertTrue(result.getGroups().isEmpty());
        assertEquals(1, result.getSkipped().size());
        SkippedUnitDTO skip = result.getSkipped().get(0);
        assertEquals(EligibilitySkipReason.UNIT_WITHOUT_ASSIGNED_EMPLOYEES.getCode(), skip.getReason());
        assertTrue(skip.getDetail().contains(orphan1.toString()));
        assertTrue(skip.getDetail().contains(orphan2.toString()));
    }

    @Test
    void individualLevel_neverConsultsResponsibleEmployeeId() {
        UUID e1 = UUID.randomUUID();
        // responsibleEmployeeId propositadamente malformado: se o ramo INDIVIDUAL_LEVEL o
        // consultasse para decidir algo, um UUID.fromString sobre este valor rebentaria.
        OrganicaDTO unit = unit(UUID.randomUUID(), "Direção J", "DJ2", "isto-nao-e-um-uuid");
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of(unit));
        when(funcionarioLookupPort.findEmployeeIdsAssignedToUnitInYear(any(), eq(PERIOD_YEAR)))
                .thenReturn(List.of(e1));
        when(funcionarioLookupPort.findAllByIds(any())).thenReturn(Map.of(e1, funcionario(e1.toString(), "Jorge")));

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.INDIVIDUAL_LEVEL));

        assertEquals(1, result.getGroups().size());
        assertTrue(result.getSkipped().isEmpty());
    }

    // --- Comuns ---

    @Test
    void noActiveUnits_returnsEmptyResultWithoutException() {
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of());

        EligibleResponsiblesDTO result = resolver.resolve(period(PaaLevel.UNIT_LEVEL));

        assertTrue(result.getGroups().isEmpty());
        assertTrue(result.getSkipped().isEmpty());
        assertEquals(0, result.getTotalEligible());
    }

    @Test
    void resultFields_comeFromPeriod() {
        PaaSubmissionPeriod period = period(PaaLevel.UNIT_LEVEL);
        when(organicaLookupPort.findAllActiveUnits()).thenReturn(List.of());

        EligibleResponsiblesDTO result = resolver.resolve(period);

        assertEquals(period.getId().toString(), result.getPeriodId());
        assertEquals(Purpose.PAA.getCode(), result.getPurpose());
        assertEquals(PaaLevel.UNIT_LEVEL.getCode(), result.getType());
        assertEquals(PERIOD_YEAR, result.getYear());
    }
}
