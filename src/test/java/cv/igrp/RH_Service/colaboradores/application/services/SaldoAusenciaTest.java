package cv.igrp.RH_Service.colaboradores.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Ciclo do saldo de ausências: submeter <b>reserva</b>, aprovar <b>confirma</b>,
 * rejeitar ou cancelar <b>devolve</b>.
 *
 * <p>Antes, aprovar apenas somava aos pendentes e os dias gozados ficavam sempre a
 * zero — o saldo disponível calhava certo, mas o histórico mentia e o cancelamento
 * de um pedido aprovado devolvia dias que nunca tinham saído.
 */
@ExtendWith(MockitoExtension.class)
class SaldoAusenciaTest {

    private static final FuncionarioId FUNCIONARIO = FuncionarioId.gerarNovo();
    private static final TipoAusenciaId TIPO = TipoAusenciaId.gerarNovo();
    private static final LocalDate INICIO = LocalDate.of(2026, 7, 1);

    @Nested
    class ContagemDosDias {

        private SaldoAusencia saldo(int direito) {
            return SaldoAusencia.criar(FUNCIONARIO, TIPO, 2026, direito);
        }

        @Test
        void reservarTiraDoDisponivelSemContarComoGozado() {
            var s = saldo(22);
            s.reservar(5);

            assertEquals(17, s.saldoDisponivel());
            assertEquals(5, s.getDiasPendentes());
            assertEquals(0, s.getDiasGozados());
        }

        @Test
        void aprovarPassaDePendenteAGozado() {
            var s = saldo(22);
            s.reservar(5);
            s.confirmarGozo(5);

            assertEquals(0, s.getDiasPendentes());
            assertEquals(5, s.getDiasGozados());
            assertEquals(17, s.saldoDisponivel());
        }

        @Test
        void libertarReservaDevolveOsDias() {
            var s = saldo(22);
            s.reservar(5);
            s.libertarReserva(5);

            assertEquals(22, s.saldoDisponivel());
            assertEquals(0, s.getDiasPendentes());
        }

        @Test
        void cancelarDepoisDeAprovadoDevolveOsDiasGozados() {
            var s = saldo(22);
            s.reservar(5);
            s.confirmarGozo(5);
            s.devolverGozo(5);

            assertEquals(22, s.saldoDisponivel());
            assertEquals(0, s.getDiasGozados());
        }

        @Test
        void naoSeReservaMaisDoQueOSaldoDisponivel() {
            var s = saldo(22);
            s.reservar(20);

            var ex = assertThrows(IgrpResponseStatusException.class, () -> s.reservar(5));
            assertEquals(422, ex.getStatusCode().value());
            assertEquals(2, s.saldoDisponivel());
        }

        @Test
        void doisPedidosEmSimultaneoNaoEsgotamDuasVezesOMesmoSaldo() {
            var s = saldo(22);
            s.reservar(15);

            assertThrows(IgrpResponseStatusException.class, () -> s.reservar(15));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class OServico {

        @Mock private TipoAusenciaRepository tipoAusenciaRepository;
        @Mock private SaldoAusenciaRepository saldoAusenciaRepository;

        @InjectMocks private SaldoAusenciaService service;

        private PedidoAusencia pedido(int dias) {
            return PedidoAusencia.criar(FUNCIONARIO, TIPO, INICIO, INICIO.plusDays(dias - 1L), dias, "férias", null);
        }

        private void tipoDesconta(boolean desconta) {
            TipoAusencia tipo = mock(TipoAusencia.class);
            when(tipo.getDeductsBalance()).thenReturn(desconta);
            when(tipoAusenciaRepository.findById(TIPO)).thenReturn(Optional.of(tipo));
        }

        private SaldoAusencia comSaldo(int direito) {
            var saldo = SaldoAusencia.criar(FUNCIONARIO, TIPO, 2026, direito);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(FUNCIONARIO, TIPO, 2026))
                    .thenReturn(Optional.of(saldo));
            return saldo;
        }

        @Test
        void tipoQueNaoDescontaNaoMexeNoSaldo() {
            tipoDesconta(false);

            service.reservar(pedido(3));

            verify(saldoAusenciaRepository, never()).save(any());
        }

        @Test
        void submeterReservaEGuarda() {
            tipoDesconta(true);
            var saldo = comSaldo(22);

            service.reservar(pedido(3));

            assertEquals(3, saldo.getDiasPendentes());
            verify(saldoAusenciaRepository).save(saldo);
        }

        @Test
        void semSaldoConfiguradoASubmissaoERecusada() {
            tipoDesconta(true);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(FUNCIONARIO, TIPO, 2026))
                    .thenReturn(Optional.empty());

            var ex = assertThrows(IgrpResponseStatusException.class, () -> service.reservar(pedido(3)));
            assertEquals(422, ex.getStatusCode().value());
        }

        @Test
        void semSaldoConfiguradoDevolverDiasNaoRebenta() {
            tipoDesconta(true);
            when(saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(FUNCIONARIO, TIPO, 2026))
                    .thenReturn(Optional.empty());

            service.libertarReserva(pedido(3));

            verify(saldoAusenciaRepository, never()).save(any());
        }
    }
}
