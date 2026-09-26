package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoPeriodoLicenca;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.models.SubtipoLicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SubtipoLicencaMobilidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.infrastructure.scheduler.LicencaEfeitoJob;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Os <b>dois eixos</b> de uma licença, e os efeitos aplicados na data em que são devidos (V48).
 *
 * <p>O art. 44.º do DL n.º 3/2010 trata dois factos em números seguidos: o n.º 1 define a licença
 * como «ausência prolongada do serviço» — um período — e o n.º 2 faz a concessão depender do
 * despacho — um acto. Enquanto os dois viviam no mesmo campo, deferir em Setembro uma licença de
 * Outubro punha a pessoa de licença em Setembro, e encerrar uma licença por começar gravava um
 * fim anterior ao início.
 */
@ExtendWith(MockitoExtension.class)
class LicencaPeriodoEfeitosTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final SubtipoLicencaMobilidadeId SUBTIPO = SubtipoLicencaMobilidadeId.gerarNovo();
    private static final LocalDate HOJE = LocalDate.now();

    @Mock private LicencaMobilidadeRepository licencaRepository;
    @Mock private SubtipoLicencaMobilidadeRepository subtipoRepository;
    @Mock private OrganizationalUnitRepository unidadeRepository;
    @Mock private LicencaService licencaService;
    @Mock private SubstituicaoService substituicaoService;

    private LicencaEfeitoService efeitoService() {
        return new LicencaEfeitoService(licencaRepository,
                new MobilidadeService(subtipoRepository, unidadeRepository, licencaRepository),
                licencaService, substituicaoService, org.mockito.Mockito.mock(DiarioFactos.class));
    }

    private static LicencaMobilidade licenca(LocalDate inicio, LocalDate fim) {
        return LicencaMobilidade.criar(FUNCIONARIO, SUBTIPO, inicio, fim,
                null, "DESP/2026", null, null, null, null, null);
    }

    private static LicencaMobilidade deferida(LocalDate inicio, LocalDate fim) {
        var l = licenca(inicio, fim);
        l.aprovar();
        return l;
    }

    private void comSubtipoNoCatalogo() {
        when(subtipoRepository.findById(any())).thenReturn(Optional.of(mock(SubtipoLicencaMobilidade.class)));
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OPeriodoDerivaDasDatas {

        @Test
        void deferidaParaOMesQueVemEstaPorIniciar() {
            var l = deferida(HOJE.plusDays(30), HOJE.plusDays(120));

            assertEquals(EstadoPeriodoLicenca.POR_INICIAR, l.estadoEm(HOJE));
            assertFalse(l.emVigorEm(HOJE));
        }

        @Test
        void aDecorrerHojeEstaEmCurso() {
            var l = deferida(HOJE.minusDays(10), HOJE.plusDays(10));

            assertEquals(EstadoPeriodoLicenca.EM_CURSO, l.estadoEm(HOJE));
            assertTrue(l.emVigorEm(HOJE));
        }

        /** O último dia ainda é licença: só termina no dia seguinte ao fim. */
        @Test
        void oUltimoDiaAindaEhLicenca() {
            var l = deferida(HOJE.minusDays(10), HOJE);

            assertEquals(EstadoPeriodoLicenca.EM_CURSO, l.estadoEm(HOJE));
            assertEquals(EstadoPeriodoLicenca.TERMINADA, l.estadoEm(HOJE.plusDays(1)));
        }

        /**
         * Sem data de fim nunca termina sozinha — é a licença de longa duração, cujo regresso
         * depende de despacho (art. 53.º) e não do calendário.
         */
        @Test
        void periodoAbertoNuncaTerminaSozinho() {
            var l = deferida(HOJE.minusDays(500), null);

            assertEquals(EstadoPeriodoLicenca.EM_CURSO, l.estadoEm(HOJE.plusYears(10)));
        }

        /** Um pedido por decidir não tem período nenhum a decorrer. */
        @Test
        void oQueNaoEstaDeferidoNaoTemPeriodo() {
            assertNull(licenca(HOJE.minusDays(10), HOJE.plusDays(10)).estadoEm(HOJE));
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OsEfeitosPertencemAoPeriodoNaoAoDespacho {

        @Test
        void deferirParaOFuturoNaoAplicaEfeitosHoje() {
            var l = deferida(HOJE.plusDays(30), HOJE.plusDays(120));

            var efeito = efeitoService().aplicarEntradaSeDevida(l, HOJE);

            assertFalse(efeito.aplicado());
            verify(licencaService, never()).aplicarEntradaEmVigor(any(), any());
            verify(licencaRepository, never()).save(any());
        }

        @Test
        void chegadoODiaDeInicioOsEfeitosSaoAplicados() {
            comSubtipoNoCatalogo();
            var l = deferida(HOJE, HOJE.plusDays(90));
            when(licencaService.aplicarEntradaEmVigor(any(), any()))
                    .thenReturn(LicencaService.EfeitoAplicado.nenhum());

            var efeito = efeitoService().aplicarEntradaSeDevida(l, HOJE);

            assertTrue(efeito.aplicado());
            verify(licencaRepository).save(l);
        }

        /** Correr o job duas vezes no mesmo dia não pode aplicar os efeitos duas vezes. */
        @Test
        void aSegundaPassagemNaoRepeteOsEfeitos() {
            comSubtipoNoCatalogo();
            var l = deferida(HOJE.minusDays(1), HOJE.plusDays(90));
            when(licencaService.aplicarEntradaEmVigor(any(), any()))
                    .thenReturn(LicencaService.EfeitoAplicado.nenhum());

            var servico = efeitoService();
            assertTrue(servico.aplicarEntradaSeDevida(l, HOJE).aplicado());
            assertFalse(servico.aplicarEntradaSeDevida(l, HOJE).aplicado());

            verify(licencaService).aplicarEntradaEmVigor(any(), any());   // uma só vez
        }

        /**
         * Se o subtipo desapareceu do catálogo não se aplica nada <b>e não se marca</b>: a
         * configuração pode voltar, e marcar aqui perderia o efeito para sempre, em silêncio.
         */
        @Test
        void subtipoAusenteNaoMarcaOEfeitoComoAplicado() {
            when(subtipoRepository.findById(any())).thenReturn(Optional.empty());
            var l = deferida(HOJE, HOJE.plusDays(90));

            assertFalse(efeitoService().aplicarEntradaSeDevida(l, HOJE).aplicado());
            assertTrue(l.carecedeEfeitoEntrada(HOJE));
            verify(licencaRepository, never()).save(any());
        }

        /**
         * O regresso é aplicado com a data de <b>fim da licença</b>, não com a do dia em que o job
         * passou. Se a aplicação esteve em baixo três dias, a substituição encerra na data certa
         * à mesma — e é dessa contagem que dependem a antiguidade e as férias proporcionais
         * (art. 47.º n.os 1 a 3).
         */
        @Test
        void oRegressoUsaADataDeFimENaoADoDiaEmQueOJobPassou() {
            comSubtipoNoCatalogo();
            LocalDate fim = HOJE.minusDays(3);
            var l = deferida(HOJE.minusDays(30), fim);

            assertTrue(efeitoService().aplicarRegressoSeDevido(l, HOJE).aplicado());

            verify(substituicaoService).encerrarPorRegressoDoTitular(FUNCIONARIO, fim);
        }

        /** O «caduca automaticamente» do art. 46.º n.º 3 não se aplica antes do fim. */
        @Test
        void oRegressoNaoSeAplicaEnquantoALicencaDecorre() {
            var l = deferida(HOJE.minusDays(10), HOJE.plusDays(10));

            assertFalse(efeitoService().aplicarRegressoSeDevido(l, HOJE).aplicado());
            verify(substituicaoService, never()).encerrarPorRegressoDoTitular(any(FuncionarioId.class), any());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class RegressoAntecipadoECancelamento {

        /**
         * Art. 46.º n.º 4: o regresso antecipado encurta o período e não mexe no despacho. A data
         * de regresso é o primeiro dia <b>de volta</b>, logo o último dia de ausência é a véspera
         * — caso contrário a pessoa apareceria de licença no dia em que voltou.
         */
        @Test
        void oRegressoAntecipadoEncurtaOPeriodoAteAVespera() {
            var l = deferida(HOJE.minusDays(10), HOJE.plusDays(80));

            l.registarRegressoAntecipado(HOJE, HOJE);

            assertEquals(HOJE.minusDays(1), l.getDataFim());
            assertFalse(l.emVigorEm(HOJE));
            assertEquals(LicencaMobilidade.APPROVED, l.getStatus());
        }

        /** Partir e regressar no mesmo dia deixa um dia — o mínimo que datas em DATE exprimem. */
        @Test
        void partirERegressarNoMesmoDiaDeixaUmDia() {
            var l = deferida(HOJE, HOJE.plusDays(80));

            l.registarRegressoAntecipado(HOJE, HOJE);

            assertEquals(HOJE, l.getDataFim());
            assertFalse(l.getDataFim().isBefore(l.getDataInicio()));
        }

        /**
         * O defeito que a V48 fecha: encerrar uma licença por começar fixava o fim em hoje, antes
         * do início. Pelo art. 44.º n.º 1 não houve ausência nenhuma — o caminho é o cancelamento.
         */
        @Test
        void naoSeEncerraOQueAindaNaoComecou() {
            var l = deferida(HOJE.plusDays(30), HOJE.plusDays(120));

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> l.registarRegressoAntecipado(HOJE, HOJE));

            assertEquals(409, ex.getStatusCode().value());
            assertEquals(HOJE.plusDays(120), l.getDataFim());
            assertFalse(l.getDataFim().isBefore(l.getDataInicio()));
        }

        @Test
        void naoSeEncerraOQueJaTerminou() {
            var l = deferida(HOJE.minusDays(30), HOJE.minusDays(5));

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> l.registarRegressoAntecipado(HOJE, HOJE));

            assertEquals(409, ex.getStatusCode().value());
        }

        @Test
        void aDataDeRegressoNaoPodeSerFutura() {
            var l = deferida(HOJE.minusDays(10), HOJE.plusDays(80));

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> l.registarRegressoAntecipado(HOJE.plusDays(5), HOJE));

            assertEquals(400, ex.getStatusCode().value());
        }

        /** Desistir de uma licença deferida que ainda não começou revoga o despacho. */
        @Test
        void cancelarValeEnquantoNaoComecar() {
            var l = deferida(HOJE.plusDays(30), HOJE.plusDays(120));

            l.cancelar(HOJE);

            assertEquals(LicencaMobilidade.CANCELLED, l.getStatus());
        }

        /** Depois de começar já há ausência gozada: a saída é o regresso antecipado. */
        @Test
        void naoSeCancelaOQueJaComecou() {
            var l = deferida(HOJE.minusDays(1), HOJE.plusDays(80));

            var ex = assertThrows(IgrpResponseStatusException.class, () -> l.cancelar(HOJE));

            assertEquals(409, ex.getStatusCode().value());
            assertEquals(LicencaMobilidade.APPROVED, l.getStatus());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OJobDiario {

        @Test
        void aplicaEntradasERegressosDevidos() {
            comSubtipoNoCatalogo();
            var aEntrar = deferida(HOJE, HOJE.plusDays(90));
            var aRegressar = deferida(HOJE.minusDays(30), HOJE.minusDays(2));
            when(licencaRepository.findEntradaPorAplicar(any())).thenReturn(List.of(aEntrar));
            when(licencaRepository.findRegressoPorAplicar(any())).thenReturn(List.of(aRegressar));
            when(licencaService.aplicarEntradaEmVigor(any(), any()))
                    .thenReturn(LicencaService.EfeitoAplicado.nenhum());

            new LicencaEfeitoJob(licencaRepository, efeitoService(), "0 15 0 * * *").executar(JobContext.para(HOJE));

            verify(licencaService).aplicarEntradaEmVigor(any(), any());
            verify(substituicaoService).encerrarPorRegressoDoTitular(FUNCIONARIO, HOJE.minusDays(2));
        }

        /** Um erro num registo não pode derrubar o lote. */
        @Test
        void umErroNumRegistoNaoDerrubaOsOutros() {
            comSubtipoNoCatalogo();
            var mau = deferida(HOJE.minusDays(1), HOJE.plusDays(90));
            var bom = deferida(HOJE, HOJE.plusDays(90));
            when(licencaRepository.findEntradaPorAplicar(any())).thenReturn(List.of(mau, bom));
            when(licencaRepository.findRegressoPorAplicar(any())).thenReturn(List.of());
            when(licencaService.aplicarEntradaEmVigor(any(), any()))
                    .thenThrow(new IllegalStateException("BD em baixo"))
                    .thenReturn(LicencaService.EfeitoAplicado.nenhum());

            new LicencaEfeitoJob(licencaRepository, efeitoService(), "0 15 0 * * *").executar(JobContext.para(HOJE));

            verify(licencaRepository).save(bom);
            verify(licencaRepository, never()).save(mau);
        }
    }
}
