package cv.igrp.RH_Service.colaboradores.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cv.igrp.RH_Service.colaboradores.domain.service.CalculadoraAntiguidade.PeriodoExcluido;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * <b>Tempo de serviço</b> — a conta que não existia em lado nenhum.
 *
 * <p>A lei manda descontar o tempo de inactividade (art. 120.º n.º 2 da Lei n.º 20/X/2023) e o
 * das licenças sem vencimento (art. 47.º n.º 1 do DL n.º 3/2010). O que torna a conta delicada
 * não é subtrair — é que a <b>mesma</b> ausência chega por dois caminhos, e somá-los desconta o
 * dobro.
 */
class CalculadoraAntiguidadeTest {

    private static final LocalDate ADMISSAO = LocalDate.of(2020, 1, 1);

    private static PeriodoExcluido periodo(String de, String ate, String motivo) {
        return new PeriodoExcluido(LocalDate.parse(de), ate == null ? null : LocalDate.parse(ate), motivo);
    }

    // -----------------------------------------------------------------------------
    @Nested
    class AContagemBase {

        /** Dias de calendário, com os dois extremos incluídos. */
        @Test
        void contaOsDoisExtremos() {
            var a = CalculadoraAntiguidade.calcular(
                    LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31), List.of());

            assertEquals(31, a.diasTotais());
            assertEquals(31, a.diasContados());
        }

        @Test
        void umAnoInteiroLeSeComoUmAno() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2020, 12, 31), List.of());

            assertEquals(1, a.anos());
            assertEquals(0, a.meses());
            assertEquals(0, a.dias());
        }

        /** Sem data, ou com a referência antes da admissão, não há antiguidade — e não rebenta. */
        @Test
        void referenciaAnteriorAAdmissaoDaZero() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2019, 6, 1), List.of());

            assertEquals(0, a.diasContados());
            assertTrue(a.periodosDescontados().isEmpty());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OsPeriodosExcluidosUnemSeNaoSeSomam {

        /**
         * O caso que obriga à união: uma licença sem vencimento de longa duração conta <b>duas
         * vezes</b> — pela situação funcional em que põe o funcionário e pelo subtipo da própria
         * licença. Somadas, descontariam 2×366 dias de um ano que só tem 366.
         */
        @Test
        void aMesmaAusenciaPorDoisCaminhosDescontaUmaVez() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2022, 12, 31), List.of(
                    periodo("2021-01-01", "2021-12-31", "Situação INACTIVIDADE_FORA_QUADRO"),
                    periodo("2021-01-01", "2021-12-31", "Licença LIC_LONGA_DURACAO")));

            assertEquals(365, a.diasDescontados());
            assertEquals(1, a.periodosDescontados().size());
            assertTrue(a.periodosDescontados().get(0).motivo().contains("Situação"));
            assertTrue(a.periodosDescontados().get(0).motivo().contains("Licença"));
        }

        /** Sobreposição parcial: o desconto é a união, não a soma. */
        @Test
        void sobreposicaoParcialDescontaAUniao() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2021, 12, 31), List.of(
                    periodo("2021-01-01", "2021-03-31", "A"),
                    periodo("2021-03-01", "2021-05-31", "B")));

            assertEquals(151, a.diasDescontados());   // 1 Jan a 31 Mai
            assertEquals(1, a.periodosDescontados().size());
        }

        /** Contíguos fundem-se: entre o fim de um e o início do outro não houve serviço. */
        @Test
        void periodosContiguosFundemSe() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2021, 12, 31), List.of(
                    periodo("2021-01-01", "2021-01-31", "A"),
                    periodo("2021-02-01", "2021-02-28", "B")));

            assertEquals(1, a.periodosDescontados().size());
            assertEquals(59, a.diasDescontados());
        }

        /** Disjuntos ficam separados — e o resultado mostra os dois. */
        @Test
        void periodosDisjuntosFicamSeparados() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2021, 12, 31), List.of(
                    periodo("2021-01-01", "2021-01-31", "A"),
                    periodo("2021-06-01", "2021-06-30", "B")));

            assertEquals(2, a.periodosDescontados().size());
            assertEquals(61, a.diasDescontados());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OsPeriodosRecortamSeAoServico {

        /** Uma licença que começou antes da admissão só desconta a parte de dentro. */
        @Test
        void oQueComecouAntesDaAdmissaoSoContaDaAdmissao() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2020, 12, 31), List.of(
                    periodo("2019-06-01", "2020-01-31", "anterior")));

            assertEquals(31, a.diasDescontados());
            assertEquals(ADMISSAO, a.periodosDescontados().get(0).inicio());
        }

        /** Um período em aberto conta até à data de referência, e não indefinidamente. */
        @Test
        void oPeriodoEmAbertoParaNaDataDeReferencia() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2020, 6, 30), List.of(
                    periodo("2020-06-01", null, "licença sem fim")));

            assertEquals(30, a.diasDescontados());
            assertEquals(LocalDate.of(2020, 6, 30), a.periodosDescontados().get(0).fim());
        }

        /** O que cai todo fora do serviço não desconta nada. */
        @Test
        void oQueEstaForaDoIntervaloNaoDesconta() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2020, 12, 31), List.of(
                    periodo("2021-01-01", "2021-06-30", "posterior")));

            assertEquals(0, a.diasDescontados());
            assertTrue(a.periodosDescontados().isEmpty());
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OResultadoEmAnosMesesEDias {

        /**
         * Os dias descontados tiram-se do fim, como se o percurso fosse contínuo: dois anos de
         * serviço com um ano de licença pelo meio lêem-se como um ano de antiguidade.
         */
        @Test
        void umAnoDeLicencaNoMeioDeTresDeixaDois() {
            var a = CalculadoraAntiguidade.calcular(ADMISSAO, LocalDate.of(2022, 12, 31), List.of(
                    periodo("2021-01-01", "2021-12-31", "licença")));

            assertEquals(1096, a.diasTotais());
            assertEquals(365, a.diasDescontados());
            assertEquals(731, a.diasContados());
            assertEquals(2, a.anos());
        }
    }
}
