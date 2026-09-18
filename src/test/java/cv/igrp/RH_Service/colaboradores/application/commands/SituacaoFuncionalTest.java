package cv.igrp.RH_Service.colaboradores.application.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.application.dto.MudarEstadoColaboradorRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.application.services.CessacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.models.EstadoContrato;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Situações administrativas perante o quadro (Lei n.º 20/X/2023, art. 117.º a 122.º).
 *
 * <p>O que se guarda é a situação de cada estado do catálogo; os efeitos derivam dela.
 * Estes testes provam as duas metades: as regras que a lei fixa vivem no enum, e a
 * mudança de estado aplica-as sem conhecer códigos de estado.
 */
@ExtendWith(MockitoExtension.class)
class SituacaoFuncionalTest {

    private static final UUID FUNCIONARIO = UUID.randomUUID();
    private static final FuncionarioId FUNCIONARIO_ID = FuncionarioId.from(FUNCIONARIO);
    private static final LocalDate DATA = LocalDate.of(2026, 10, 31);

    @Nested
    class EfeitosDaLei {

        @Test
        void soAInactividadeForaDoQuadroAbreVaga() {
            // Art. 121.º n.º 2.
            assertTrue(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO.abreVaga());
            assertFalse(SituacaoFuncional.INACTIVIDADE_NO_QUADRO.abreVaga());
            assertFalse(SituacaoFuncional.ACTIVIDADE_FORA_QUADRO.abreVaga());
            assertFalse(SituacaoFuncional.DISPONIBILIDADE.abreVaga());
        }

        @Test
        void aInactividadeNaoContaParaAntiguidadeEADisponibilidadeConta() {
            // Art. 120.º n.º 2 e art. 122.º n.º 1.
            assertFalse(SituacaoFuncional.INACTIVIDADE_NO_QUADRO.contaAntiguidade());
            assertFalse(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO.contaAntiguidade());
            assertTrue(SituacaoFuncional.DISPONIBILIDADE.contaAntiguidade());
            assertTrue(SituacaoFuncional.ACTIVIDADE_NO_QUADRO.contaAntiguidade());
        }

        @Test
        void soAAposentacaoCessaOVinculo() {
            // Art. 93.º al. b.
            assertTrue(SituacaoFuncional.APOSENTACAO.cessaVinculo());
            assertFalse(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO.cessaVinculo());
        }

        @Test
        void recusaSituacaoQueNaoEstaNaLei() {
            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> SituacaoFuncional.de("FERIAS"));
            assertEquals(422, ex.getStatusCode().value());
        }

        @Test
        void semSituacaoEValidoEEstadoFicaPorClassificar() {
            assertNull(SituacaoFuncional.de(null));
            assertNull(SituacaoFuncional.de("  "));
        }
    }

    @Nested
    class CoerenciaDoCatalogo {

        @Test
        void estadoDeCessacaoSoAceitaAposentacaoOuNenhumaSituacao() {
            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> WorkerState.criar("EXONERADO", "Exonerado", false, true, "DISPONIBILIDADE"));
            assertEquals(422, ex.getStatusCode().value());

            assertNotNull(WorkerState.criar("RETIRED", "Aposentado", false, true, "APOSENTACAO"));
            assertNotNull(WorkerState.criar("INACTIVE", "Inactivo", true, true, null));
        }

        @Test
        void aposentacaoTemDeCessarOVinculo() {
            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> WorkerState.criar("RETIRED", "Aposentado", false, false, "APOSENTACAO"));
            assertEquals(422, ex.getStatusCode().value());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class EfeitosDaMudancaDeEstado {

        @Mock private FuncionarioRepository funcionarioRepository;
        @Mock private WorkerStateRepository workerStateRepository;
        @Mock private ContratoRepository contratoRepository;
        @Mock private HistoricoEstadoColaboradorRepository historicoRepository;
        @Mock private CessacaoService cessacaoService;
        @Mock private AssignmentService assignmentService;

        @InjectMocks private MudarEstadoColaboradorCommandHandler handler;

        private static WorkerState estado(String code, SituacaoFuncional situacao) {
            return WorkerState.reconstruir(WorkerStateId.gerarNovo(), code, code, false, true, false, situacao);
        }

        private MudarEstadoColaboradorCommand command(WorkerState novoEstado) {
            var dto = new MudarEstadoColaboradorRequestDTO();
            dto.setWorkerStateId(novoEstado.getId().getStringValor());
            dto.setDataEfectividade(DATA);
            return new MudarEstadoColaboradorCommand(FUNCIONARIO.toString(), dto);
        }

        private void cenario(WorkerState novoEstado) {
            Funcionario funcionario = mock(Funcionario.class);
            when(funcionario.getWorkerStateId()).thenReturn(UUID.randomUUID());
            when(funcionarioRepository.findById(FUNCIONARIO_ID)).thenReturn(Optional.of(funcionario));
            when(workerStateRepository.findById(any())).thenReturn(Optional.of(novoEstado));
        }

        private Contrato contrato(EstadoContrato status) {
            Contrato contrato = mock(Contrato.class);
            when(contrato.getStatus()).thenReturn(status);
            when(contrato.getId()).thenReturn(ContratoId.from(UUID.randomUUID()));
            when(contratoRepository.findCurrentByFuncionarioId(FUNCIONARIO_ID)).thenReturn(Optional.of(contrato));
            return contrato;
        }

        @Test
        void inactividadeForaDoQuadroAbreVagaESuspendeOContrato() {
            WorkerState fora = estado("INACTIVE_OUTSIDE", SituacaoFuncional.INACTIVIDADE_FORA_QUADRO);
            cenario(fora);
            Contrato contrato = contrato(EstadoContrato.ATIVO);
            Assignment afectacao = mock(Assignment.class);
            UUID afectacaoId = UUID.randomUUID();
            when(afectacao.getId()).thenReturn(AssignmentId.from(afectacaoId));
            when(assignmentService.encerrarAfectacaoCorrente(FUNCIONARIO_ID, DATA))
                    .thenReturn(Optional.of(afectacao));

            var response = handler.handle(command(fora));

            assertEquals(200, response.getStatusCode().value());
            assertFalse(response.getBody().getCessouVinculo());
            assertEquals(afectacaoId.toString(), response.getBody().getAfectacaoEncerradaId());
            verify(contrato).suspender();
            // A vaga abre, mas o vínculo não cessa: isso é caminho do CessacaoService.
            verify(cessacaoService, never()).cessar(any(), any(), any(), any(), any());
        }

        @Test
        void inactividadeNoQuadroSuspendeOContratoSemAbrirVaga() {
            WorkerState suspenso = estado("SUSPENDED", SituacaoFuncional.INACTIVIDADE_NO_QUADRO);
            cenario(suspenso);
            Contrato contrato = contrato(EstadoContrato.ATIVO);

            var response = handler.handle(command(suspenso));

            verify(contrato).suspender();
            assertNull(response.getBody().getAfectacaoEncerradaId());
            verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), any());
        }

        @Test
        void actividadeForaDoQuadroMantemOLugarEOContrato() {
            // Art. 119.º: comissão de serviço, requisição, cargos políticos.
            WorkerState fora = estado("ACTIVE_OUTSIDE", SituacaoFuncional.ACTIVIDADE_FORA_QUADRO);
            cenario(fora);
            Contrato contrato = contrato(EstadoContrato.ATIVO);

            handler.handle(command(fora));

            verify(contrato, never()).suspender();
            verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), any());
        }

        @Test
        void regressoAActividadeReactivaOContrato() {
            WorkerState activo = estado("ACTIVE", SituacaoFuncional.ACTIVIDADE_NO_QUADRO);
            cenario(activo);
            Contrato contrato = contrato(EstadoContrato.SUSPENSO);

            handler.handle(command(activo));

            verify(contrato).reativar();
            verify(historicoRepository).save(any(HistoricoEstadoColaborador.class));
        }

        @Test
        void disponibilidadeReactivaOContratoPorqueOFuncionarioPrestaServico() {
            // Art. 122.º n.º 2: na disponibilidade presta serviço e é abonado.
            WorkerState disponivel = estado("AVAILABLE", SituacaoFuncional.DISPONIBILIDADE);
            cenario(disponivel);
            Contrato contrato = contrato(EstadoContrato.SUSPENSO);

            handler.handle(command(disponivel));

            verify(contrato).reativar();
        }

        @Test
        void estadoSemSituacaoClassificadaSoRegistaHistorico() {
            WorkerState porClassificar = estado("CUSTOM", null);
            cenario(porClassificar);
            // Sem situação nem se chega a olhar para o estado do contrato.
            Contrato contrato = mock(Contrato.class);
            when(contrato.getId()).thenReturn(ContratoId.from(UUID.randomUUID()));
            when(contratoRepository.findCurrentByFuncionarioId(FUNCIONARIO_ID)).thenReturn(Optional.of(contrato));

            handler.handle(command(porClassificar));

            verify(contrato, never()).suspender();
            verify(contrato, never()).reativar();
            verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), any());
            verify(historicoRepository).save(any(HistoricoEstadoColaborador.class));
        }

        @Test
        void aposentacaoContinuaAPassarPeloCaminhoUnicoDaCessacao() {
            WorkerState aposentado = WorkerState.reconstruir(WorkerStateId.gerarNovo(), "RETIRED", "RETIRED",
                    false, true, true, SituacaoFuncional.APOSENTACAO);
            cenario(aposentado);
            when(cessacaoService.cessar(eq(FUNCIONARIO_ID), eq(aposentado), eq(DATA), any(), any()))
                    .thenReturn(new CessacaoService.Cessacao(mock(Funcionario.class), aposentado,
                            UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), DATA));

            var response = handler.handle(command(aposentado));

            assertTrue(response.getBody().getCessouVinculo());
            verify(assignmentService, never()).encerrarAfectacaoCorrente(any(), any());
        }
    }
}
