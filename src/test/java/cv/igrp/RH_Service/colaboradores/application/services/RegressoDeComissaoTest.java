package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Prova do regresso de comissão de serviço — Lei n.º 20/X/2023, art. 64.º n.º 2: «Cessada a
 * comissão de serviço, o nomeado regressa à situação jurídico-funcional de que era titular antes
 * dela, quando constituída e consolidada por tempo indeterminado, ou, <b>no caso contrário, cessa
 * a relação jurídica de emprego público</b>.»
 *
 * <p>Até aqui o catálogo classificava a comissão como {@code REGRESSA_LUGAR} sem condição, e o
 * regresso devolvia ao Lugar de origem <b>toda a gente</b> — incluindo quem foi recrutado
 * <i>para</i> a comissão e nunca teve Lugar nenhum.
 *
 * <p>Qual dos dois caminhos se segue <b>deriva-se do percurso</b>: a comissão mantém o Lugar,
 * logo quem tinha situação anterior continua a ser titular dele.
 */
@ExtendWith(MockitoExtension.class)
class RegressoDeComissaoTest {

    @Mock private AssignmentService assignmentService;
    @Mock private CessacaoService cessacaoService;
    @Mock private AssignmentRepository assignmentRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private HistoricoEstadoColaboradorRepository historicoRepository;

    @InjectMocks private LicencaService service;

    private final FuncionarioId funcionarioId = FuncionarioId.gerarNovo();
    private final LocalDate dataRegresso = LocalDate.of(2026, 9, 30);

    private static SubtipoLicencaMobilidade comissao() {
        return SubtipoLicencaMobilidade.reconstituir(
                SubtipoLicencaMobilidadeId.gerarNovo(), "Comissão de Serviço", "MOB_COMISSAO",
                "MOBILIDADE", false, true, false, true, 1095, null,
                "MANTEM", null, "REGRESSA_OU_CESSA");
    }

    private LicencaMobilidade licenca(SubtipoLicencaMobilidade subtipo) {
        return LicencaMobilidade.criar(funcionarioId, subtipo.getId(),
                LocalDate.of(2023, 10, 1), dataRegresso,
                null, null, null, null, UUID.randomUUID(), null, null);
    }

    private Assignment afectacaoCorrente() {
        return Assignment.criar(funcionarioId, UUID.randomUUID(), UUID.randomUUID(), null,
                TipoAfectacao.PRINCIPAL, Assignment.ADMISSAO, LocalDate.of(2020, 1, 1), null);
    }

    /**
     * Quem tinha Lugar continua a tê-lo — a comissão mantém-no. Regressar é não fazer nada:
     * não há estado a mudar nem vínculo a cessar.
     */
    @Test
    void quemTinhaSituacaoAnteriorRegressaESemEfeitoNenhum() {
        var subtipo = comissao();
        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.of(afectacaoCorrente()));

        var efeito = service.aplicarRegresso(licenca(subtipo), subtipo, dataRegresso);

        assertTrue(efeito.isEmpty());
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
    }

    /** Quem foi recrutado PARA a comissão não tem para onde voltar: a relação cessa. */
    @Test
    void quemNaoTinhaSituacaoAnteriorVeOVinculoCessar() {
        var subtipo = comissao();
        var estadoCessacao = mock(WorkerState.class);
        var idEstado = WorkerStateId.gerarNovo();
        when(estadoCessacao.getId()).thenReturn(idEstado);

        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.empty());
        when(cessacaoService.estadoDeCessacaoPorOmissao()).thenReturn(estadoCessacao);
        when(cessacaoService.cessar(eq(funcionarioId), eq(estadoCessacao), eq(dataRegresso), any(), any()))
                .thenReturn(new CessacaoService.Cessacao(null, estadoCessacao, null, null, null, dataRegresso));

        var efeito = service.aplicarRegresso(licenca(subtipo), subtipo, dataRegresso);

        assertEquals(idEstado.getValor(), efeito.orElseThrow());
    }

    /** A cessação fica no histórico a dizer porquê — não como um desaparecimento silencioso. */
    @Test
    void aCessacaoDizPorqueAconteceu() {
        var subtipo = comissao();
        var estadoCessacao = mock(WorkerState.class);
        when(estadoCessacao.getId()).thenReturn(WorkerStateId.gerarNovo());

        when(assignmentRepository.findCurrentPrincipalByFuncionario(funcionarioId))
                .thenReturn(Optional.empty());
        when(cessacaoService.estadoDeCessacaoPorOmissao()).thenReturn(estadoCessacao);
        when(cessacaoService.cessar(any(), any(), any(), any(), any()))
                .thenReturn(new CessacaoService.Cessacao(null, estadoCessacao, null, null, null, dataRegresso));

        service.aplicarRegresso(licenca(subtipo), subtipo, dataRegresso);

        ArgumentCaptor<String> observacao = ArgumentCaptor.forClass(String.class);
        verify(cessacaoService).cessar(any(), any(), any(), any(), observacao.capture());
        assertTrue(observacao.getValue().contains("64"));
    }

    /**
     * Uma mobilidade comum não passa por aqui: mantém sempre o Lugar e o regresso não produz
     * efeito nenhum. É o que já era, e continua a ser.
     */
    @Test
    void umaMobilidadeComumContinuaSemEfeitoNoRegresso() {
        var subtipo = SubtipoLicencaMobilidade.reconstituir(
                SubtipoLicencaMobilidadeId.gerarNovo(), "Requisição", "MOB_REQUISICAO",
                "MOBILIDADE", false, true, false, true, 365, 1,
                "MANTEM", null, "REGRESSA_LUGAR");

        var efeito = service.aplicarRegresso(licenca(subtipo), subtipo, dataRegresso);

        assertTrue(efeito.isEmpty());
        verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        verify(assignmentRepository, never()).findCurrentPrincipalByFuncionario(any());
    }
}
