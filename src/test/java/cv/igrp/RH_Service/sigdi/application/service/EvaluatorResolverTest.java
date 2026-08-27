package cv.igrp.RH_Service.sigdi.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.sigdi.application.constants.EvaluatorSkipReason;
import cv.igrp.RH_Service.sigdi.application.port.OrganicaLookupPort;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Testes de {@link EvaluatorResolver}: as três regras de derivação do avaliador travadas
 * pelo operador a 2026-08-27, e os desfechos degradados de cada uma. Identificadores gerados
 * sempre por {@link UUID#randomUUID()} -- nunca cadeias como {@code "U1"}, porque um id de
 * teste não-UUID rebentaria em runtime antes de qualquer mock ser tocado (tropeço registado
 * no 116-03-SUMMARY.md).
 */
@ExtendWith(MockitoExtension.class)
class EvaluatorResolverTest {

    @Mock
    private OrganicaLookupPort organicaLookupPort;

    @InjectMocks
    private EvaluatorResolver evaluatorResolver;

    @Test
    void regra1DevolveOResponsavelDaUnidadeQuandoNaoEOProprioENaoConsultaAUnidadeMae() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID responsibleId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(responsibleId));

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertFalse(resolution.isSkipped());
        assertEquals(responsibleId, resolution.evaluatorId());
        assertNull(resolution.skipReason());
        verify(organicaLookupPort, never()).findParentUnitId(unitId);
    }

    @Test
    void regra2SobeAUnidadeMaeQuandoOResponsavelEOProprioColaborador() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID parentUnitId = UUID.randomUUID();
        UUID parentResponsibleId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(employeeId));
        when(organicaLookupPort.findParentUnitId(unitId)).thenReturn(Optional.of(parentUnitId));
        when(organicaLookupPort.findResponsibleEmployeeId(parentUnitId)).thenReturn(Optional.of(parentResponsibleId));

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertFalse(resolution.isSkipped());
        assertEquals(parentResponsibleId, resolution.evaluatorId());
        assertNull(resolution.skipReason());
    }

    @Test
    void regra3SaltaComTopUnitHeadQuandoAUnidadeNaoTemMae() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(employeeId));
        when(organicaLookupPort.findParentUnitId(unitId)).thenReturn(Optional.empty());

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertTrue(resolution.isSkipped());
        assertNull(resolution.evaluatorId());
        assertEquals(EvaluatorSkipReason.TOP_UNIT_HEAD, resolution.skipReason());
    }

    @Test
    void regra2DegradadaSaltaComParentUnitWithoutResponsibleQuandoAMaeNaoTemResponsavel() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID parentUnitId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(employeeId));
        when(organicaLookupPort.findParentUnitId(unitId)).thenReturn(Optional.of(parentUnitId));
        when(organicaLookupPort.findResponsibleEmployeeId(parentUnitId)).thenReturn(Optional.empty());

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertTrue(resolution.isSkipped());
        assertEquals(EvaluatorSkipReason.PARENT_UNIT_WITHOUT_RESPONSIBLE, resolution.skipReason());
    }

    @Test
    void regra2DegradadaSaltaComParentUnitNotFoundQuandoAMaeApontaParaUnidadeInexistente() {
        // Com a assinatura de findParentUnitId, este caso e indistinguivel de "mae sem
        // responsavel" a partir do proprio resolvedor: findResponsibleEmployeeId(parentId)
        // devolve Optional.empty() nos dois casos, para um id que FOI devolvido por
        // findParentUnitId. O desfecho observavel e por isso o mesmo,
        // PARENT_UNIT_WITHOUT_RESPONSIBLE -- PARENT_UNIT_NOT_FOUND fica reservado para o
        // dia em que essa distincao se torne possivel (ex.: um findById na propria porta).
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID parentUnitId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(employeeId));
        when(organicaLookupPort.findParentUnitId(unitId)).thenReturn(Optional.of(parentUnitId));
        when(organicaLookupPort.findResponsibleEmployeeId(parentUnitId)).thenReturn(Optional.empty());

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertTrue(resolution.isSkipped());
        assertEquals(EvaluatorSkipReason.PARENT_UNIT_WITHOUT_RESPONSIBLE, resolution.skipReason());
    }

    @Test
    void regra2EncadeadaNaoExisteSaltaComParentUnitResponsibleIsSelfENaoSobeSegundaVez() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID parentUnitId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(employeeId));
        when(organicaLookupPort.findParentUnitId(unitId)).thenReturn(Optional.of(parentUnitId));
        when(organicaLookupPort.findResponsibleEmployeeId(parentUnitId)).thenReturn(Optional.of(employeeId));

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertTrue(resolution.isSkipped());
        assertEquals(EvaluatorSkipReason.PARENT_UNIT_RESPONSIBLE_IS_SELF, resolution.skipReason());
        verify(organicaLookupPort, never()).findParentUnitId(parentUnitId);
    }

    @Test
    void unidadeSemResponsavelSaltaComUnitWithoutResponsibleENaoConsultaUnidadeMae() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.empty());

        EvaluatorResolver.EvaluatorResolution resolution = evaluatorResolver.resolve(employeeId, unitId);

        assertTrue(resolution.isSkipped());
        assertEquals(EvaluatorSkipReason.UNIT_WITHOUT_RESPONSIBLE, resolution.skipReason());
        verify(organicaLookupPort, never()).findParentUnitId(unitId);
    }

    @Test
    void employeeIdNuloLancaIllegalArgumentException() {
        UUID unitId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> evaluatorResolver.resolve(null, unitId));
    }

    @Test
    void unitIdNuloLancaIllegalArgumentException() {
        UUID employeeId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> evaluatorResolver.resolve(employeeId, null));
    }

    @Test
    void nenhumResultadoTemAvaliadorEMotivoSimultaneamenteNemNenhumTemOsDoisVazios() {
        UUID employeeId = UUID.randomUUID();
        UUID unitId = UUID.randomUUID();
        UUID responsibleId = UUID.randomUUID();

        when(organicaLookupPort.findResponsibleEmployeeId(unitId)).thenReturn(Optional.of(responsibleId));
        EvaluatorResolver.EvaluatorResolution withEvaluator = evaluatorResolver.resolve(employeeId, unitId);
        assertTrue((withEvaluator.evaluatorId() != null) ^ (withEvaluator.skipReason() != null));

        UUID otherUnitId = UUID.randomUUID();
        when(organicaLookupPort.findResponsibleEmployeeId(otherUnitId)).thenReturn(Optional.empty());
        EvaluatorResolver.EvaluatorResolution withSkip = evaluatorResolver.resolve(employeeId, otherUnitId);
        assertTrue((withSkip.evaluatorId() != null) ^ (withSkip.skipReason() != null));
    }
}
