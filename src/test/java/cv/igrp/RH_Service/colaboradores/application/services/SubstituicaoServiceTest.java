package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AssignmentId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

/**
 * Prova da substituição de funcionário temporariamente impedido (Lei n.º 20/X/2023,
 * art. 73.º al. a) a c) e art. 91.º n.º 1 al. a)).
 *
 * <p>O que se afirma aqui: quem substitui entra sem desalojar o titular; só se
 * substitui quem está mesmo impedido, e isso lê-se da <b>situação funcional</b> do
 * seu estado e não de uma lista de códigos; e a substituição está amarrada à
 * afectação do titular, que é o que a faz caducar (art. 77.º n.º 2).
 */
@ExtendWith(MockitoExtension.class)
class SubstituicaoServiceTest {

    @Mock private AssignmentRepository assignmentRepository;
    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private PositionRepository positionRepository;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private AssignmentService assignmentService;

    @InjectMocks private SubstituicaoService service;

    private final FuncionarioId substitutoId = FuncionarioId.gerarNovo();
    private final FuncionarioId titularId = FuncionarioId.gerarNovo();
    private final UUID positionId = UUID.randomUUID();
    private final UUID workerStateId = UUID.randomUUID();
    private final LocalDate inicioTitular = LocalDate.of(2024, 1, 1);
    private final LocalDate inicio = LocalDate.of(2026, 3, 1);

    /** Lugar fora de grelha: mantém o teste focado no impedimento, sem escalões pelo meio. */
    private Position lugar() {
        return Position.reconstituir(PositionId.from(positionId), "L-01", UUID.randomUUID(),
                UUID.randomUUID(), null, null, null, null, Position.ATIVO, null, true);
    }

    private Assignment afectacaoDoTitular() {
        return Assignment.reconstituir(AssignmentId.gerarNovo(), titularId, positionId, null, null,
                TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null,
                inicioTitular, null, true, true, null);
    }

    private Funcionario titular() {
        Funcionario f = Mockito.mock(Funcionario.class);
        when(f.getWorkerStateId()).thenReturn(workerStateId);
        return f;
    }

    private void estadoDoTitularEm(SituacaoFuncional situacao) {
        WorkerState estado = Mockito.mock(WorkerState.class);
        when(estado.situacao()).thenReturn(Optional.ofNullable(situacao));
        when(workerStateRepository.findById(WorkerStateId.from(workerStateId)))
                .thenReturn(Optional.of(estado));
    }

    private void cenario(SituacaoFuncional situacaoDoTitular, Assignment afectacaoTitular) {
        // O mock do titular constroi-se ANTES do when(...) que o devolve: criar um mock com
        // stubs dentro do argumento de thenReturn deixa o Mockito com um when() por fechar.
        Funcionario titular = titular();
        when(positionRepository.findById(PositionId.from(positionId))).thenReturn(Optional.of(lugar()));
        when(assignmentRepository.findTitularByPosition(positionId)).thenReturn(Optional.of(afectacaoTitular));
        when(funcionarioRepository.findById(titularId)).thenReturn(Optional.of(titular));
        estadoDoTitularEm(situacaoDoTitular);
    }

    private IgrpResponseStatusException erroAoSubstituir() {
        return assertThrows(IgrpResponseStatusException.class,
                () -> service.substituir(substitutoId, positionId, null, null, inicio, null));
    }

    @Nested
    class SoSeSubstituiQuemEstaImpedido {

        @Test
        void aInactividadeNoQuadroPermiteSubstituir() {
            Assignment doTitular = afectacaoDoTitular();
            cenario(SituacaoFuncional.INACTIVIDADE_NO_QUADRO, doTitular);
            when(assignmentRepository.findSubstitutoCorrente(doTitular.getId())).thenReturn(Optional.empty());
            when(assignmentService.afectarSubstituicao(any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(i -> Assignment.criarSubstituicao(substitutoId, positionId, null, null,
                            doTitular.getId(), inicio, null));

            var resultado = service.substituir(substitutoId, positionId, null, null, inicio, null);

            assertTrue(resultado.afectacao().isSubstituicao());
            assertEquals(doTitular.getId().getValor(), resultado.afectacao().getTitularAssignmentId());
            verify(assignmentService).afectarSubstituicao(
                    eq(substitutoId), eq(positionId), eq(null), eq(null),
                    eq(doTitular.getId()), eq(inicio), eq(null));
        }

        @Test
        void aActividadeForaDoQuadroTambemPermite() {
            Assignment doTitular = afectacaoDoTitular();
            cenario(SituacaoFuncional.ACTIVIDADE_FORA_QUADRO, doTitular);
            when(assignmentRepository.findSubstitutoCorrente(doTitular.getId())).thenReturn(Optional.empty());
            when(assignmentService.afectarSubstituicao(any(), any(), any(), any(), any(), any(), any()))
                    .thenAnswer(i -> Assignment.criarSubstituicao(substitutoId, positionId, null, null,
                            doTitular.getId(), inicio, null));

            service.substituir(substitutoId, positionId, null, null, inicio, null);

            verify(assignmentService).afectarSubstituicao(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void quemEstaEmFuncoesNaoSeSubstitui() {
            cenario(SituacaoFuncional.ACTIVIDADE_NO_QUADRO, afectacaoDoTitular());

            var erro = erroAoSubstituir();

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erro.getStatusCode());
            verify(assignmentService, never()).afectarSubstituicao(any(), any(), any(), any(), any(), any(), any());
        }

        @Test
        void umEstadoSemSituacaoClassificadaNaoChega() {
            // O catálogo é da instituição, mas sem situação não se sabe se está impedido:
            // a aplicação recusa em vez de adivinhar.
            cenario(null, afectacaoDoTitular());

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erroAoSubstituir().getStatusCode());
        }
    }

    @Nested
    class OQueTornaOPedidoImpossivel {

        @Test
        void umLugarVagoProveSeComTitularENaoComSubstituto() {
            when(positionRepository.findById(PositionId.from(positionId))).thenReturn(Optional.of(lugar()));
            when(assignmentRepository.findTitularByPosition(positionId)).thenReturn(Optional.empty());

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erroAoSubstituir().getStatusCode());
        }

        @Test
        void ninguemSeSubstituiASiProprio() {
            Assignment doProprio = Assignment.reconstituir(AssignmentId.gerarNovo(), substitutoId, positionId,
                    null, null, TipoAfectacao.PRINCIPAL.name(), Assignment.ADMISSAO, null,
                    inicioTitular, null, true, true, null);
            when(positionRepository.findById(PositionId.from(positionId))).thenReturn(Optional.of(lugar()));
            when(assignmentRepository.findTitularByPosition(positionId)).thenReturn(Optional.of(doProprio));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erroAoSubstituir().getStatusCode());
        }

        @Test
        void umTitularSoTemUmSubstitutoDeCadaVez() {
            Assignment doTitular = afectacaoDoTitular();
            cenario(SituacaoFuncional.INACTIVIDADE_NO_QUADRO, doTitular);
            when(assignmentRepository.findSubstitutoCorrente(doTitular.getId()))
                    .thenReturn(Optional.of(Assignment.criarSubstituicao(FuncionarioId.gerarNovo(), positionId,
                            null, null, doTitular.getId(), inicio, null)));

            assertEquals(HttpStatus.CONFLICT, erroAoSubstituir().getStatusCode());
        }

        @Test
        void aSubstituicaoNaoComecaAntesDaAfectacaoDoTitular() {
            Assignment doTitular = afectacaoDoTitular();
            cenario(SituacaoFuncional.INACTIVIDADE_NO_QUADRO, doTitular);
            when(assignmentRepository.findSubstitutoCorrente(doTitular.getId())).thenReturn(Optional.empty());

            var erro = assertThrows(IgrpResponseStatusException.class, () -> service.substituir(
                    substitutoId, positionId, null, null, inicioTitular.minusDays(1), null));

            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, erro.getStatusCode());
        }
    }

    @Nested
    class OFimDaSubstituicao {

        @Test
        void oRegressoDoTitularEncerraQuemOSubstituia() {
            Assignment doTitular = afectacaoDoTitular();
            Assignment substituicao = Assignment.criarSubstituicao(substitutoId, positionId, null, null,
                    doTitular.getId(), inicio, null);
            when(assignmentRepository.findCurrentPrincipalByFuncionario(titularId))
                    .thenReturn(Optional.of(doTitular));
            when(assignmentRepository.findSubstituicoesCorrentes(doTitular.getId()))
                    .thenReturn(List.of(substituicao));
            when(assignmentRepository.save(any(Assignment.class))).thenAnswer(i -> i.getArgument(0));

            LocalDate regresso = LocalDate.of(2026, 9, 30);
            var encerradas = service.encerrarPorRegressoDoTitular(titularId, regresso);

            assertEquals(1, encerradas.size());
            assertEquals(regresso, substituicao.getDataFim());
            assertEquals(Boolean.FALSE, substituicao.getIsCurrent());
        }

        @Test
        void semSubstitutoNaoFazNada() {
            Assignment doTitular = afectacaoDoTitular();
            when(assignmentRepository.findCurrentPrincipalByFuncionario(titularId))
                    .thenReturn(Optional.of(doTitular));
            when(assignmentRepository.findSubstituicoesCorrentes(doTitular.getId())).thenReturn(List.of());

            assertTrue(service.encerrarPorRegressoDoTitular(titularId, LocalDate.of(2026, 9, 30)).isEmpty());
            verify(assignmentRepository, never()).save(any());
        }

        @Test
        void semAfectacaoDoTitularNaoHaNadaQueEncerrar() {
            // É o caso de quem já tinha perdido o Lugar: não há substituição ligada a nada.
            when(assignmentRepository.findCurrentPrincipalByFuncionario(titularId)).thenReturn(Optional.empty());

            assertTrue(service.encerrarPorRegressoDoTitular(titularId, LocalDate.of(2026, 9, 30)).isEmpty());
            verify(assignmentRepository, never()).save(any());
        }
    }
}
