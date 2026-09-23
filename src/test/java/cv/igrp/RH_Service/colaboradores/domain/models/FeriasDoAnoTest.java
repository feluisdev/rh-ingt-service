package cv.igrp.RH_Service.colaboradores.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Marcação de férias — DL n.º 3/2010, arts. 5.º e 6.º. */
class FeriasDoAnoTest {

    private static final int ANO = 2027;
    private static final LocalDate HOJE = LocalDate.of(2027, 2, 10);

    private static RegrasMarcacaoFerias regras(Integer direito, boolean anoDeIngresso) {
        return new RegrasMarcacaoFerias(direito, 22, 11, anoDeIngresso,
                LocalDate.of(ANO, 5, 1), LocalDate.of(ANO, 10, 31));
    }

    private static PeriodoFerias p(String inicio, String fim, int uteis) {
        return new PeriodoFerias(LocalDate.parse(inicio), LocalDate.parse(fim), uteis);
    }

    private static PeriodoFerias p(String inicio, String fim) {
        return new PeriodoFerias(LocalDate.parse(inicio), LocalDate.parse(fim), null);
    }

    private static FeriasDoAno novas() {
        return FeriasDoAno.novo(FuncionarioId.gerarNovo(), ANO);
    }

    private static int status(Runnable r) {
        return assertThrows(IgrpResponseStatusException.class, r::run).getBody().getStatus();
    }

    @Nested
    class Preferencia {

        @Test
        void dentroDoPrazoSemAlerta() {
            var f = novas();
            var alertas = f.indicarPreferencia(List.of(p("2027-08-02", "2027-08-31")), null,
                    LocalDate.of(2027, 1, 20), LocalDate.of(2027, 1, 31));
            assertTrue(alertas.isEmpty());
            assertFalse(f.isPreferenciaForaDePrazo());
            assertTrue(f.temPreferencia());
        }

        @Test
        void foraDoPrazoEAceiteComAlerta_art5n4() {
            var f = novas();
            var alertas = f.indicarPreferencia(List.of(p("2027-08-02", "2027-08-31")), null,
                    LocalDate.of(2027, 2, 1), LocalDate.of(2027, 1, 31));
            assertEquals(1, alertas.size());
            assertTrue(f.isPreferenciaForaDePrazo());
        }

        @Test
        void periodosDeOutroAnoOuSobrepostosSao422() {
            var f = novas();
            assertEquals(422, status(() -> f.indicarPreferencia(List.of(p("2026-12-20", "2027-01-05")), null, HOJE, HOJE)));
            assertEquals(422, status(() -> f.indicarPreferencia(
                    List.of(p("2027-08-02", "2027-08-20"), p("2027-08-15", "2027-08-31")), null, HOJE, HOJE)));
            assertEquals(422, status(() -> f.indicarPreferencia(List.of(), null, HOJE, HOJE)));
        }
    }

    @Nested
    class Marcacao {

        @Test
        void porAcordoNumSoPeriodo() {
            var f = novas();
            var alertas = f.marcar(List.of(p("2027-08-02", "2027-08-31", 22)), OrigemMarcacaoFerias.ACORDO,
                    null, regras(22, false), false, null, HOJE);
            assertTrue(alertas.isEmpty());
            assertEquals(22, f.totalMarcado());
        }

        @Test
        void maisDoQueODireitoE422() {
            var f = novas();
            assertEquals(422, status(() -> f.marcar(List.of(p("2027-08-02", "2027-08-31", 22)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(20, false), false, null, HOJE)));
        }

        @Test
        void aMenosDoQueODireitoAvisaQueFicamDiasPorMarcar() {
            var f = novas();
            var alertas = f.marcar(List.of(p("2027-08-02", "2027-08-13", 10)), OrigemMarcacaoFerias.ACORDO,
                    null, regras(22, false), false, null, HOJE);
            assertEquals(1, alertas.size());
        }

        @Test
        void seguidasNaoPassamDoDireitoAnual_art5n1() {
            var f = novas();
            assertEquals(422, status(() -> f.marcar(List.of(p("2027-07-01", "2027-08-31", 23)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(30, false), false, null, HOJE)));
        }

        @Test
        void interpoladoPrecisaDeUmPeriodoDeOnzeDias_art5n1() {
            var f = novas();
            assertEquals(422, status(() -> f.marcar(
                    List.of(p("2027-07-05", "2027-07-16", 10), p("2027-08-02", "2027-08-13", 10)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(22, false), false, null, HOJE)));
            // com um de 11 passa
            f.marcar(List.of(p("2027-07-05", "2027-07-19", 11), p("2027-08-02", "2027-08-16", 11)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(22, false), false, null, HOJE);
            assertEquals(22, f.totalMarcado());
        }

        @Test
        void noAnoDeIngressoOMinimoNaoSeAplica_art3() {
            var f = novas();
            f.marcar(List.of(p("2027-07-05", "2027-07-09", 5), p("2027-08-02", "2027-08-06", 5)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(11, true), false, null, HOJE);
            assertEquals(10, f.totalMarcado());
        }

        @Test
        void comDireitoMenorDoQueOnzeOMinimoNaoSeExige() {
            var f = novas();
            f.marcar(List.of(p("2027-07-05", "2027-07-09", 5), p("2027-08-02", "2027-08-04", 3)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(8, false), false, null, HOJE);
            assertEquals(8, f.totalMarcado());
        }

        @Test
        void fixadaSoEntreMaioEOutubro_art5n5() {
            var f = novas();
            assertEquals(422, status(() -> f.marcar(List.of(p("2027-12-01", "2027-12-31", 22)),
                    OrigemMarcacaoFerias.FIXADA, null, regras(22, false), false, null, HOJE)));
            f.marcar(List.of(p("2027-08-02", "2027-08-31", 22)), OrigemMarcacaoFerias.FIXADA,
                    null, regras(22, false), false, null, HOJE);
            assertEquals(OrigemMarcacaoFerias.FIXADA, f.getOrigem());
        }

        @Test
        void fixarInterpoladoSemFundamentacaoE422_art5n2() {
            var f = novas();
            var periodos = List.of(p("2027-06-01", "2027-06-15", 11), p("2027-09-01", "2027-09-15", 11));
            assertEquals(422, status(() -> f.marcar(periodos, OrigemMarcacaoFerias.FIXADA, null,
                    regras(22, false), false, null, HOJE)));
            f.marcar(periodos, OrigemMarcacaoFerias.FIXADA, "Época alta de atendimento em Agosto",
                    regras(22, false), false, null, HOJE);
            assertEquals(2, f.getMarcacao().size());
        }

        @Test
        void interpoladoPorAcordoNaoPrecisaDeFundamentacao() {
            var f = novas();
            f.marcar(List.of(p("2027-06-01", "2027-06-15", 11), p("2027-09-01", "2027-09-15", 11)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(22, false), false, null, HOJE);
            assertEquals(2, f.getMarcacao().size());
        }

        @Test
        void periodoSemDiasUteisE422() {
            var f = novas();
            assertEquals(422, status(() -> f.marcar(List.of(p("2027-08-07", "2027-08-08", 0)),
                    OrigemMarcacaoFerias.ACORDO, null, regras(22, false), false, null, HOJE)));
        }

        @Test
        void semDireitoConhecidoMarcaEAvisa() {
            var f = novas();
            var alertas = f.marcar(List.of(p("2027-08-02", "2027-08-31", 22)), OrigemMarcacaoFerias.ACORDO,
                    null, regras(null, false), false, null, HOJE);
            assertEquals(1, alertas.size());
        }
    }

    @Nested
    class DepoisDePublicado {

        private FeriasDoAno marcadaEPublicada() {
            var f = novas();
            f.marcar(List.of(p("2027-08-02", "2027-08-31", 22)), OrigemMarcacaoFerias.ACORDO,
                    null, regras(22, false), false, null, HOJE);
            return f;
        }

        private final List<PeriodoFerias> outra = List.of(p("2027-09-01", "2027-09-30", 22));

        @Test
        void alterarSemMotivoE422_art6n2() {
            var f = marcadaEPublicada();
            assertEquals(422, status(() -> f.marcar(outra, OrigemMarcacaoFerias.ACORDO, null,
                    regras(22, false), true, null, HOJE)));
        }

        @Test
        void porAcordoAlteraERegista() {
            var f = marcadaEPublicada();
            f.marcar(outra, OrigemMarcacaoFerias.ACORDO, null, regras(22, false), true,
                    MotivoAlteracaoMapaFerias.ACORDO, HOJE);
            assertEquals(1, f.getAlteracoes().size());
            var a = f.getAlteracoes().get(0);
            assertEquals(MotivoAlteracaoMapaFerias.ACORDO, a.motivo());
            assertTrue(a.periodosAnteriores().startsWith("2027-08-02"));
            assertTrue(a.periodosNovos().startsWith("2027-09-01"));
        }

        @Test
        void porConvenienciaSemFundamentacaoE422() {
            var f = marcadaEPublicada();
            assertEquals(422, status(() -> f.marcar(outra, OrigemMarcacaoFerias.ACORDO, null,
                    regras(22, false), true, MotivoAlteracaoMapaFerias.CONVENIENCIA_SERVICO, HOJE)));
        }

        @Test
        void aPrimeiraMarcacaoDepoisDePublicadoNaoEAlteracao() {
            var f = novas();
            f.marcar(outra, OrigemMarcacaoFerias.ACORDO, null, regras(22, false), true, null, HOJE);
            assertTrue(f.getAlteracoes().isEmpty());
        }
    }
}
