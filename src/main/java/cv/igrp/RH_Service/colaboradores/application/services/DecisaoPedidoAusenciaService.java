package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * <b>Quem decide um pedido de ausência</b> — a hierarquia: o «superior hierárquico imediato» e o
 * «dirigente» que autoriza (DL n.º 3/2010, arts. 9.º e 15.º). A <b>chefia directa</b> (titular do
 * Lugar-pai de quem pediu) decide pela sua caixa ({@code /me/equipa}); o <b>RH</b> decide sempre (e é
 * o caminho quando a chefia está vaga ou não está definida). Um só nível: a decisão é final.
 *
 * <p>Os efeitos no saldo são os mesmos pelos dois caminhos: aprovar passa os dias reservados a
 * gozados; rejeitar devolve-os.
 */
@Service
@RequiredArgsConstructor
public class DecisaoPedidoAusenciaService {

    private final PedidoAusenciaRepository pedidoRepository;
    private final SaldoAusenciaService saldoAusenciaService;
    private final ChefiaService chefiaService;
    private final AvisosAusencia avisosAusencia;

    /**
     * {@code chefe} presente: decide a chefia directa, e fica ela como decisora. {@code chefe} nulo:
     * decide o RH, e fica {@code decisorRh} (o que o RH indicar). {@code funcionarioId} (do caminho do
     * RH) tem de ser o dono do pedido — senão 404.
     */
    @Transactional
    public PedidoAusencia aprovar(FuncionarioId chefe, FuncionarioId funcionarioId, PedidoAusenciaId id,
                                  FuncionarioId decisorRh, String observacoes) {
        var pedido = encontrar(funcionarioId, id);
        if (chefe != null) exigirChefiaDe(chefe, pedido.getFuncionarioId());
        pedido.aprovar(chefe != null ? chefe : decisorRh, LocalDate.now(), texto(observacoes));
        saldoAusenciaService.confirmarGozo(pedido);
        var gravado = pedidoRepository.save(pedido);
        avisosAusencia.pedidoDecidido(gravado);
        return gravado;
    }

    /** Como {@link #aprovar}; pela chefia, rejeitar exige motivo. */
    @Transactional
    public PedidoAusencia rejeitar(FuncionarioId chefe, FuncionarioId funcionarioId, PedidoAusenciaId id,
                                   FuncionarioId decisorRh, String motivo) {
        var pedido = encontrar(funcionarioId, id);
        if (chefe != null) {
            exigirChefiaDe(chefe, pedido.getFuncionarioId());
            if (texto(motivo) == null) throw invalido("Rejeitar um pedido de ausência exige motivo.");
        }
        pedido.rejeitar(chefe != null ? chefe : decisorRh, LocalDate.now(), texto(motivo));
        saldoAusenciaService.libertarReserva(pedido);
        var gravado = pedidoRepository.save(pedido);
        avisosAusencia.pedidoDecidido(gravado);
        return gravado;
    }

    /** Os pedidos por decidir da equipa directa de uma chefia. */
    @Transactional(readOnly = true)
    public List<PedidoAusencia> pendentesDaEquipa(FuncionarioId chefe) {
        return pedidoRepository.findPendentesDe(chefiaService.equipaDirecta(chefe));
    }

    private void exigirChefiaDe(FuncionarioId chefe, FuncionarioId dono) {
        if (chefe.equals(dono)) throw invalido("Ninguém decide os seus próprios pedidos de ausência.");
        if (!chefiaService.eChefeDirecto(chefe, dono))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
                    "Só a chefia directa de quem pediu (ou o RH) decide este pedido.");
    }

    private PedidoAusencia encontrar(FuncionarioId funcionarioId, PedidoAusenciaId id) {
        return pedidoRepository.findById(id)
                .filter(p -> funcionarioId == null || p.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Pedido não encontrado: " + id.getStringValor()));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
