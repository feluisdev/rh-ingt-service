package cv.igrp.RH_Service.sigdi.infrastructure.lookup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova de {@link FuncionarioLookupAdapter#findEmployeeIdsAssignedToUnitInYear(UUID, int)}.
 *
 * <p>Cobre os cinco casos do bloco {@code behavior} do {@code 116-02-PLAN.md}: três
 * funcionários distintos, desduplicação de um funcionário com dois enquadramentos no
 * mesmo ano e unidade, lista vazia (nunca {@code null}), ordem estável de primeira
 * ocorrência, e delegação em {@code findAllByUnidadeOrganicaIdCoveringYear} em vez de
 * {@code findCurrentByFuncionarioId}.
 */
@ExtendWith(MockitoExtension.class)
class FuncionarioLookupAdapterTest {

    private static final UUID UNIDADE_ORGANICA_ID = UUID.randomUUID();
    private static final int YEAR = 2027;

    @Mock
    private FuncionarioRepository repository;

    @Mock
    private EnquadramentoRepository enquadramentoRepository;

    @InjectMocks
    private FuncionarioLookupAdapter adapter;

    private static EnquadramentoProfissional enquadramento(UUID funcionarioId) {
        return EnquadramentoProfissional.reconstituir(
                EnquadramentoId.gerarNovo(),
                FuncionarioId.from(funcionarioId),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UNIDADE_ORGANICA_ID,
                LocalDate.of(YEAR, 1, 1),
                null,
                true);
    }

    @Test
    void findEmployeeIdsAssignedToUnitInYear_comTresFuncionariosDistintos_devolveOsTres() {
        UUID f1 = UUID.randomUUID();
        UUID f2 = UUID.randomUUID();
        UUID f3 = UUID.randomUUID();
        when(enquadramentoRepository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, YEAR))
                .thenReturn(List.of(enquadramento(f1), enquadramento(f2), enquadramento(f3)));

        List<UUID> resultado = adapter.findEmployeeIdsAssignedToUnitInYear(UNIDADE_ORGANICA_ID, YEAR);

        assertEquals(List.of(f1, f2, f3), resultado);
    }

    @Test
    void findEmployeeIdsAssignedToUnitInYear_comDoisEnquadramentosDoMesmoFuncionario_desduplicaComOrdemPreservada() {
        UUID f1 = UUID.randomUUID();
        UUID f2 = UUID.randomUUID();
        when(enquadramentoRepository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, YEAR))
                .thenReturn(List.of(enquadramento(f1), enquadramento(f2), enquadramento(f1)));

        List<UUID> resultado = adapter.findEmployeeIdsAssignedToUnitInYear(UNIDADE_ORGANICA_ID, YEAR);

        assertEquals(List.of(f1, f2), resultado);
    }

    @Test
    void findEmployeeIdsAssignedToUnitInYear_semEnquadramentoACobrirOAno_devolveListaVaziaNuncaNull() {
        when(enquadramentoRepository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, YEAR))
                .thenReturn(List.of());

        List<UUID> resultado = adapter.findEmployeeIdsAssignedToUnitInYear(UNIDADE_ORGANICA_ID, YEAR);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void findEmployeeIdsAssignedToUnitInYear_comUnitIdNulo_devolveListaVaziaSemChamarRepositorio() {
        List<UUID> resultado = adapter.findEmployeeIdsAssignedToUnitInYear(null, YEAR);

        assertTrue(resultado.isEmpty());
        verify(enquadramentoRepository, never())
                .findAllByUnidadeOrganicaIdCoveringYear(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void findEmployeeIdsAssignedToUnitInYear_delegaEmFindAllByUnidadeOrganicaIdCoveringYearNaoEmFindCurrent() {
        UUID f1 = UUID.randomUUID();
        when(enquadramentoRepository.findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, YEAR))
                .thenReturn(List.of(enquadramento(f1)));

        adapter.findEmployeeIdsAssignedToUnitInYear(UNIDADE_ORGANICA_ID, YEAR);

        verify(enquadramentoRepository).findAllByUnidadeOrganicaIdCoveringYear(UNIDADE_ORGANICA_ID, YEAR);
        verify(enquadramentoRepository, never()).findCurrentByFuncionarioId(org.mockito.ArgumentMatchers.any());
    }
}
