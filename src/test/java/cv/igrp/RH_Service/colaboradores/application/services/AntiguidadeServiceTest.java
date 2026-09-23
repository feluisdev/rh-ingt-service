package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <b>Antiguidade</b> — prova que as colunas que ninguém lia passaram a ser lidas.
 *
 * <p>Antes disto, {@code SituacaoFuncional.contaAntiguidade()} não tinha consumidor e
 * {@code counts_for_seniority} podia ser parametrizado sem efeito nenhum: quem o configurasse
 * ficava convencido de que tinha feito alguma coisa.
 */
@ExtendWith(MockitoExtension.class)
class AntiguidadeServiceTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final LocalDate ADMISSAO = LocalDate.of(2020, 1, 1);
    private static final LocalDate REFERENCIA = LocalDate.of(2022, 12, 31);

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private HistoricoEstadoColaboradorRepository historicoRepository;
    @Mock private WorkerStateRepository workerStateRepository;
    @Mock private LicencaMobilidadeRepository licencaRepository;
    @Mock private SubtipoLicencaMobilidadeRepository subtipoRepository;
    @Mock private ContratoRepository contratoRepository;
    @Mock private ContractTypeRepository contractTypeRepository;
    @Mock private VinculoLaboralRepository vinculoLaboralRepository;
    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository pedidoAusenciaRepository;
    @Mock private cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository tipoAusenciaRepository;

    private AntiguidadeService servico() {
        // A ordem segue a dos campos do servico: o pedido de ausencia e o tipo entram a seguir ao
        // contrato, que e onde a quarta fonte da antiguidade foi declarada (V54).
        return new AntiguidadeService(funcionarioRepository, historicoRepository, workerStateRepository,
                licencaRepository, subtipoRepository, contratoRepository,
                pedidoAusenciaRepository, tipoAusenciaRepository,
                contractTypeRepository, vinculoLaboralRepository);
    }

    private void admitidoEm(LocalDate data) {
        Funcionario f = mock(Funcionario.class);
        lenient().when(f.getDataAdmissao()).thenReturn(data);
        when(funcionarioRepository.findById(FUNCIONARIO)).thenReturn(Optional.of(f));
    }

    /** Por omissão, nada a descontar: sem histórico, sem licenças, sem contratos. */
    private void semNadaAMais() {
        lenient().when(historicoRepository.findAllByFuncionarioId(any())).thenReturn(List.of());
        lenient().when(licencaRepository.findAllByFuncionarioId(any(), any())).thenReturn(List.of());
        lenient().when(contratoRepository.findAllByFuncionarioIdOrderByStartDateDesc(any()))
                .thenReturn(List.of());
        lenient().when(pedidoAusenciaRepository.findAllByFuncionarioId(any(), any()))
                .thenReturn(List.of());
    }

    /**
     * Um pedido de ausência de um tipo classificado no regime indicado. O regime é o que
     * distingue uma falta injustificada de qualquer outra ausência — nunca o código.
     */
    private void pedidoDe(cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia regime,
                          String codigo, String estado, LocalDate inicio, LocalDate fim) {
        var tipoId = cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId.gerarNovo();
        var tipo = cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia.reconstituir(
                tipoId, codigo, codigo, false, false, null, null, null, "PESSOAL", true, regime, null, null);
        lenient().when(tipoAusenciaRepository.findById(tipoId)).thenReturn(Optional.of(tipo));

        var pedido = cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia.reconstituir(
                cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId.gerarNovo(),
                FUNCIONARIO, tipoId, inicio, fim,
                (int) java.time.temporal.ChronoUnit.DAYS.between(inicio, fim) + 1,
                "motivo", estado, null, null, null, true, null, null, null);

        lenient().when(pedidoAusenciaRepository.findAllByFuncionarioId(any(), any()))
                .thenReturn(List.of(pedido));
    }

    private UUID estadoCom(SituacaoFuncional situacao, String codigo) {
        UUID id = UUID.randomUUID();
        WorkerState estado = WorkerState.reconstruir(WorkerStateId.from(id), codigo, codigo,
                false, true, false, situacao);
        lenient().when(workerStateRepository.findById(WorkerStateId.from(id)))
                .thenReturn(Optional.of(estado));
        return id;
    }

    private static HistoricoEstadoColaborador transicao(UUID paraEstado, LocalDate quando) {
        return HistoricoEstadoColaborador.criar(FUNCIONARIO, null, paraEstado, "MOTIVO", quando, null);
    }

    // -----------------------------------------------------------------------------
    @Nested
    class ASituacaoFuncionalPassaAContar {

        /** Art. 120.º n.º 2: o tempo de inactividade no quadro não conta. */
        @Test
        void aInactividadeNoQuadroDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            UUID inactivo = estadoCom(SituacaoFuncional.INACTIVIDADE_NO_QUADRO, "SUSPENDED");
            UUID activo = estadoCom(SituacaoFuncional.ACTIVIDADE_NO_QUADRO, "ACTIVE");
            when(historicoRepository.findAllByFuncionarioId(any())).thenReturn(List.of(
                    transicao(inactivo, LocalDate.of(2021, 1, 1)),
                    transicao(activo, LocalDate.of(2022, 1, 1))));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(365, a.diasDescontados());
            assertEquals(731, a.diasContados());
        }

        /** Art. 122.º n.º 1: a disponibilidade conta expressamente. */
        @Test
        void aDisponibilidadeConta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            UUID disponivel = estadoCom(SituacaoFuncional.DISPONIBILIDADE, "AVAILABLE");
            when(historicoRepository.findAllByFuncionarioId(any()))
                    .thenReturn(List.of(transicao(disponivel, LocalDate.of(2021, 1, 1))));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(0, a.diasDescontados());
        }

        /**
         * Um estado sem situação classificada é configuração em falta, e não um estado que não
         * conta: descontar por omissão tiraria tempo a quem o tem.
         */
        @Test
        void estadoSemSituacaoNaoDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            UUID semSituacao = estadoCom(null, "POR_CLASSIFICAR");
            when(historicoRepository.findAllByFuncionarioId(any()))
                    .thenReturn(List.of(transicao(semSituacao, LocalDate.of(2021, 1, 1))));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(0, a.diasDescontados());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OCatalogoDasLicencasPassaAContar {

        private LicencaMobilidade licenca(LocalDate de, LocalDate ate, boolean deferida) {
            var l = LicencaMobilidade.criar(FUNCIONARIO, SubtipoLicencaMobilidadeId.gerarNovo(),
                    de, ate, null, "DESP", null, null, null, null, null);
            if (deferida) l.aprovar();
            return l;
        }

        private void comSubtipo(String codigo, boolean contaAntiguidade, boolean mobilidade) {
            SubtipoLicencaMobilidade s = mock(SubtipoLicencaMobilidade.class);
            lenient().when(s.getCodigo()).thenReturn(codigo);
            lenient().when(s.getCountsForSeniority()).thenReturn(contaAntiguidade);
            lenient().when(s.isMobilidade()).thenReturn(mobilidade);
            when(subtipoRepository.findById(any())).thenReturn(Optional.of(s));
        }

        /** Art. 47.º n.º 1: a licença sem vencimento desconta na antiguidade. */
        @Test
        void aLicencaClassificadaDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            comSubtipo("LIC_SEM_VENC", false, false);
            when(licencaRepository.findAllByFuncionarioId(any(), any())).thenReturn(
                    List.of(licenca(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31), true)));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(365, a.diasDescontados());
            assertTrue(a.periodosDescontados().get(0).motivo().contains("LIC_SEM_VENC"));
        }

        /** Um pedido que não chegou a ser deferido não produziu ausência nenhuma. */
        @Test
        void aLicencaPorDecidirNaoDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            when(licencaRepository.findAllByFuncionarioId(any(), any())).thenReturn(
                    List.of(licenca(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31), false)));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(0, a.diasDescontados());
        }

        /**
         * Art. 137.º da Lei n.º 20/X/2023: na mobilidade o tempo conta no lugar de origem. Não é
         * configurável — mesmo que o subtipo esteja mal classificado, não desconta.
         */
        @Test
        void aMobilidadeNuncaDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            comSubtipo("MOB_REQ", false, true);
            when(licencaRepository.findAllByFuncionarioId(any(), any())).thenReturn(
                    List.of(licenca(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31), true)));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(0, a.diasDescontados());
        }

        /**
         * O caso que obriga à união: a mesma licença chega pela situação funcional e pelo
         * subtipo. Somadas, descontariam o dobro dos dias que o ano tem.
         */
        @Test
        void aMesmaAusenciaPelosDoisCaminhosDescontaUmaVez() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            UUID foraDoQuadro = estadoCom(SituacaoFuncional.INACTIVIDADE_FORA_QUADRO, "INACTIVE_OUTSIDE");
            UUID activo = estadoCom(SituacaoFuncional.ACTIVIDADE_NO_QUADRO, "ACTIVE");
            when(historicoRepository.findAllByFuncionarioId(any())).thenReturn(List.of(
                    transicao(foraDoQuadro, LocalDate.of(2021, 1, 1)),
                    transicao(activo, LocalDate.of(2022, 1, 1))));
            comSubtipo("LIC_LONGA_DURACAO", false, false);
            when(licencaRepository.findAllByFuncionarioId(any(), any())).thenReturn(
                    List.of(licenca(LocalDate.of(2021, 1, 1), LocalDate.of(2021, 12, 31), true)));

            var a = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(365, a.diasDescontados());
            assertEquals(1, a.periodosDescontados().size());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class QuandoNaoHaPorOndeComecar {

        @Test
        void colaboradorInexistenteDa404() {
            when(funcionarioRepository.findById(any())).thenReturn(Optional.empty());

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> servico().calcular(FUNCIONARIO, REFERENCIA));

            assertEquals(404, ex.getStatusCode().value());
        }

        /** Sem data de admissão não há contagem possível — e diz-se, em vez de devolver zero. */
        @Test
        void semDataDeAdmissaoDa422() {
            admitidoEm(null);

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> servico().calcular(FUNCIONARIO, REFERENCIA));

            assertEquals(422, ex.getStatusCode().value());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class AFaltaInjustificadaNaoConta {

        /**
         * Art. 43.º n.º 2: «As faltas injustificadas [...] não contam para efeitos de
         * antiguidade.» É imperativo — a instituição diz QUAIS das suas linhas o são, pelo
         * regime, mas não pode decidir que contam.
         */
        @Test
        void oPeriodoDaFaltaInjustificadaEDescontado() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            pedidoDe(cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia.FALTA_INJUSTIFICADA,
                    "FALTA_INJUSTIFICADA", "APROVADO",
                    LocalDate.of(2021, 3, 1), LocalDate.of(2021, 3, 10));

            var antiguidade = servico().calcular(FUNCIONARIO, REFERENCIA);

            assertEquals(10, antiguidade.diasDescontados());
        }

        /** Uma falta justificada não desconta nada: o eixo é o regime, não o facto de faltar. */
        @Test
        void aFaltaJustificadaNaoDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            pedidoDe(cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia.FALTA,
                    "DOENCA", "APROVADO", LocalDate.of(2021, 3, 1), LocalDate.of(2021, 3, 10));

            assertEquals(0, servico().calcular(FUNCIONARIO, REFERENCIA).diasDescontados());
        }

        /**
         * Um pedido rejeitado ou cancelado não produziu ausência nenhuma — mesmo critério com
         * que se somam os dias para os tectos do art. 15.º.
         */
        @Test
        void oPedidoRejeitadoNaoDesconta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            pedidoDe(cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia.FALTA_INJUSTIFICADA,
                    "FALTA_INJUSTIFICADA", "REJEITADO",
                    LocalDate.of(2021, 3, 1), LocalDate.of(2021, 3, 10));

            assertEquals(0, servico().calcular(FUNCIONARIO, REFERENCIA).diasDescontados());
        }

        /**
         * Tipo por classificar <b>conta</b>. É configuração em falta, não uma falta
         * injustificada: descontar por omissão tiraria tempo a quem o tem.
         */
        @Test
        void oTipoSemRegimeClassificadoConta() {
            admitidoEm(ADMISSAO);
            semNadaAMais();
            pedidoDe(null, "POR_CLASSIFICAR", "APROVADO",
                    LocalDate.of(2021, 3, 1), LocalDate.of(2021, 3, 10));

            assertEquals(0, servico().calcular(FUNCIONARIO, REFERENCIA).diasDescontados());
        }
    }
}
