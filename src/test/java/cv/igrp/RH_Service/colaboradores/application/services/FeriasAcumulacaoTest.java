package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.RegimeAusencia;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * <b>Acumulação de férias para o ano seguinte</b> — DL n.º 3/2010, art. 7.º n.º 1.
 *
 * <p>Depois de o saldo passar a nascer sozinho (V49), os dias não gozados ficavam na linha do ano
 * anterior sem caminho nenhum: um pedido de 2027 só olha para o saldo de 2027. A lei não os deixa
 * cair — mas também não os arrasta sozinha: só há acumulação quando, «por motivo de serviço», não
 * puderam ser gozados nesse ano.
 */
@ExtendWith(MockitoExtension.class)
class FeriasAcumulacaoTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final TipoAusenciaId TIPO_FERIAS = TipoAusenciaId.gerarNovo();
    private static final String MOTIVO = "Acumulacao por conveniencia de servico";

    @Mock private FuncionarioRepository funcionarioRepository;
    @Mock private TipoAusenciaRepository tipoAusenciaRepository;
    @Mock private SaldoAusenciaRepository saldoAusenciaRepository;

    private FeriasService servico() {
        return new FeriasService(funcionarioRepository, tipoAusenciaRepository, saldoAusenciaRepository);
    }

    private static TipoAusencia tipo(RegimeAusencia regime) {
        return TipoAusencia.reconstituir(TIPO_FERIAS, "Férias", "FERIAS", true, true,
                22, null, null, "GOZAMENTO", true, regime, null, null);
    }

    private static SaldoAusencia saldo(int ano, int direito) {
        return SaldoAusencia.criar(FUNCIONARIO, TIPO_FERIAS, ano, direito);
    }

    private void comTipo(RegimeAusencia regime) {
        when(tipoAusenciaRepository.findById(any())).thenReturn(Optional.of(tipo(regime)));
    }

    private void comDestino(SaldoAusencia destino) {
        when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(any(), any(), anyInt()))
                .thenReturn(Optional.of(destino));
    }

    private void comDestinoEGravacao(SaldoAusencia destino) {
        comDestino(destino);
        when(saldoAusenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    // -----------------------------------------------------------------------------
    @Nested
    class OsDiasPassamParaOAnoSeguinte {

        @Test
        void oQueSobraDeUmAnoFicaDisponivelNoSeguinte() {
            comTipo(RegimeAusencia.FERIAS);
            var origem = saldo(2026, 22);
            origem.reservar(10);
            origem.confirmarGozo(10);              // 12 por gozar
            var destino = saldo(2027, 22);
            comDestinoEGravacao(destino);

            var resultado = servico().acumularParaOAnoSeguinte(origem, 12, MOTIVO);

            assertEquals(12, resultado.getDiasAcumulados());
            assertEquals(MOTIVO, resultado.getAcumulacaoMotivo());
            assertEquals(34, resultado.saldoDisponivel());   // 22 do ano + 12 acumulados
        }

        /** Os dias cedidos deixam de contar na origem — senão existiriam nos dois anos. */
        @Test
        void osDiasCedidosSaemDoAnoDeOrigem() {
            comTipo(RegimeAusencia.FERIAS);
            var origem = saldo(2026, 22);
            comDestinoEGravacao(saldo(2027, 22));

            servico().acumularParaOAnoSeguinte(origem, 22, MOTIVO);

            assertEquals(0, origem.saldoDisponivel());
            assertEquals(22, origem.getDiasTransportados());
        }

        @Test
        void naoSeAcumulaMaisDoQueSobra() {
            comTipo(RegimeAusencia.FERIAS);
            var origem = saldo(2026, 22);
            origem.reservar(20);                    // 2 por gozar
            comDestino(saldo(2027, 22));

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> servico().acumularParaOAnoSeguinte(origem, 5, MOTIVO));

            assertEquals(422, ex.getStatusCode().value());
        }

        /** Acumular duas vezes os mesmos dias seria multiplicá-los. */
        @Test
        void naoSeAcumulamDuasVezesOsMesmosDias() {
            comTipo(RegimeAusencia.FERIAS);
            var origem = saldo(2026, 22);
            comDestinoEGravacao(saldo(2027, 22));
            var s = servico();

            s.acumularParaOAnoSeguinte(origem, 15, MOTIVO);

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> s.acumularParaOAnoSeguinte(origem, 10, MOTIVO));
            assertEquals(422, ex.getStatusCode().value());
        }

        /**
         * O horizonte da lei é de um ano: o art. 7.º n.º 1 fala do «ano seguinte» e o art. 8.º
         * n.º 4 manda gozar o remanescente «até ao termo do ano civil imediato». Dias já
         * acumulados não se voltam a acumular.
         */
        @Test
        void osDiasRecebidosNaoSeVoltamAAcumular() {
            var recebeu = saldo(2027, 22);
            recebeu.receberAcumulados(10, MOTIVO);
            recebeu.reservar(22);
            recebeu.confirmarGozo(22);              // gastou o direito do proprio ano

            assertEquals(10, recebeu.saldoDisponivel());   // os acumulados continuam la
            assertEquals(0, recebeu.diasAcumulaveis());    // mas nao seguem para 2028
        }
    }

    // -----------------------------------------------------------------------------
    @Nested
    class ALeiExigeMotivoEEhSoParaFerias {

        /** Art. 7.º n.º 1: só «por motivo de serviço». Sem motivo não há acumulação. */
        @Test
        void semMotivoNaoHaAcumulacao() {
            var origem = saldo(2026, 22);

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> servico().acumularParaOAnoSeguinte(origem, 5, "  "));

            assertEquals(400, ex.getStatusCode().value());
            verify(saldoAusenciaRepository, never()).save(any());
        }

        /** A acumulação é do capítulo das férias. Uma falta não se acumula. */
        @Test
        void umaFaltaNaoSeAcumula() {
            comTipo(RegimeAusencia.FALTA);
            var origem = saldo(2026, 5);

            var ex = assertThrows(IgrpResponseStatusException.class,
                    () -> servico().acumularParaOAnoSeguinte(origem, 2, MOTIVO));

            assertEquals(422, ex.getStatusCode().value());
        }

        /** Autorizada em Dezembro, antes de o job do vencimento ter criado o saldo do ano novo. */
        @Test
        void oSaldoDoAnoDeDestinoEhCriadoSeAindaNaoExistir() {
            comTipo(RegimeAusencia.FERIAS);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(any(), any(), anyInt()))
                    .thenReturn(Optional.empty());
            when(tipoAusenciaRepository.findFerias()).thenReturn(Optional.of(tipo(RegimeAusencia.FERIAS)));
            Funcionario f = mock(Funcionario.class);
            when(f.getDataAdmissao()).thenReturn(LocalDate.of(2020, 1, 1));
            when(funcionarioRepository.findById(any())).thenReturn(Optional.of(f));
            when(saldoAusenciaRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            var origem = saldo(2026, 22);
            var destino = servico().acumularParaOAnoSeguinte(origem, 4, MOTIVO);

            assertEquals(2027, destino.getAno());
            assertEquals(4, destino.getDiasAcumulados());
            assertEquals(26, destino.saldoDisponivel());
        }
    }
}
