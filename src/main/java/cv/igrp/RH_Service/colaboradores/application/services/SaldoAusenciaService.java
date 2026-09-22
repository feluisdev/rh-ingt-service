package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Movimento do saldo de ausências ao longo da vida de um pedido.
 *
 * <p>O saldo tem três números: os dias a que se tem direito, os <b>reservados</b>
 * (pedidos à espera de decisão) e os <b>gozados</b>. Antes, aprovar um pedido
 * apenas somava aos reservados, e os gozados ficavam eternamente a zero — o saldo
 * disponível dava o valor certo por acaso, mas o histórico não dizia nada e o
 * cancelamento de um pedido aprovado devolvia dias que nunca tinham saído.
 *
 * <p>O percurso passa a ser: submeter <b>reserva</b> · aprovar <b>confirma</b> ·
 * rejeitar ou cancelar antes da decisão <b>liberta</b> · cancelar depois de
 * aprovado <b>devolve</b>.
 *
 * <p>Só os tipos com {@code deducts_balance} têm saldo; os outros passam ao lado.
 */
@Service
@RequiredArgsConstructor
public class SaldoAusenciaService {

    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final SaldoAusenciaRepository saldoAusenciaRepository;

    /** Submissão: reserva os dias, recusando o pedido se não houver saldo. */
    @Transactional
    public void reservar(PedidoAusencia pedido) {
        aplicar(pedido, true, saldo -> saldo.reservar(pedido.getNumeroDias()));
    }

    /** Aprovação: os dias reservados passam a gozados. */
    @Transactional
    public void confirmarGozo(PedidoAusencia pedido) {
        aplicar(pedido, false, saldo -> saldo.confirmarGozo(pedido.getNumeroDias()));
    }

    /** Rejeição, ou cancelamento antes da decisão: liberta a reserva. */
    @Transactional
    public void libertarReserva(PedidoAusencia pedido) {
        aplicar(pedido, false, saldo -> saldo.libertarReserva(pedido.getNumeroDias()));
    }

    /** Cancelamento depois de aprovado: devolve os dias gozados. */
    @Transactional
    public void devolverGozo(PedidoAusencia pedido) {
        aplicar(pedido, false, saldo -> saldo.devolverGozo(pedido.getNumeroDias()));
    }

    /**
     * Devolve <b>parte</b> dos dias gozados. É o caso da suspensão de férias (art. 8.º): o
     * período encurta, uma parte foi mesmo gozada e o resto volta ao saldo. Ao contrário do
     * cancelamento, o número não é o do pedido — que entretanto já foi ajustado — mas o que
     * deixou de ser gozado.
     */
    @Transactional
    public void devolverDias(PedidoAusencia pedido, int dias) {
        if (dias <= 0) return;
        aplicar(pedido, false, saldo -> saldo.devolverGozo(dias));
    }

    /**
     * @param exigirSaldo na submissão, não haver saldo configurado é motivo para
     *                    recusar; a devolver dias, não — não se prende um
     *                    cancelamento por falta de configuração
     */
    private void aplicar(PedidoAusencia pedido, boolean exigirSaldo, Consumer<SaldoAusencia> movimento) {
        boolean desconta = tipoAusenciaRepository.findById(pedido.getTipoAusenciaId())
                .map(tipo -> Boolean.TRUE.equals(tipo.getDeductsBalance()))
                .orElse(false);
        if (!desconta) return;

        Optional<SaldoAusencia> saldo = saldoAusenciaRepository.findByFuncionarioIdAndTipoAusenciaIdAndAno(
                pedido.getFuncionarioId(), pedido.getTipoAusenciaId(), pedido.getDataInicio().getYear());

        if (saldo.isEmpty()) {
            if (exigirSaldo)
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Não existe saldo de ausência para o funcionário, tipo e ano do pedido.");
            return;
        }

        movimento.accept(saldo.get());
        saldoAusenciaRepository.save(saldo.get());
    }
}
