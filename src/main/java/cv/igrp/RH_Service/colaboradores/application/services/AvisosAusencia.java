package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Quem se avisa nos pedidos de ausência, e com que texto (BR-NOT-04):
 * <ul>
 *   <li>pedido por decidir → a <b>chefia directa</b> de quem pediu; sem chefia (Lugar-pai vago ou
 *       inexistente), a caixa do <b>RH</b>, que é quem decide nesse caso;</li>
 *   <li>pedido decidido (aprovado ou rejeitado) → <b>quem pediu</b>.</li>
 * </ul>
 * Os que nascem aprovados (tipos sem aprovação) não avisam ninguém: não há nada a decidir.
 */
@Service
@RequiredArgsConstructor
public class AvisosAusencia {

    static final String RECURSO = "PEDIDO_AUSENCIA";

    private final Notificador notificador;
    private final ChefiaService chefiaService;
    private final FuncionarioRepository funcionarioRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;

    public void pedidoCriado(PedidoAusencia pedido) {
        if (pedido.getEstado() == null || !pedido.getEstado().isPendente()) return;
        String quem = funcionarioRepository.findById(pedido.getFuncionarioId())
                .map(Funcionario::getNomeCompleto).orElse("Um colaborador");
        String titulo = quem + " pediu " + nomeDoTipo(pedido) + " " + Datas.periodo(pedido.getDataInicio(), pedido.getDataFim());
        var chefe = chefiaService.chefeDirecto(pedido.getFuncionarioId());
        var envio = chefe.isPresent() ? notificador.para(chefe) : notificador.paraRh();
        envio.tipo(TipoNotificacao.PEDIDO_AUSENCIA_PENDENTE)
                .titulo(titulo)
                .texto("O pedido aguarda a sua decisão.")
                .recurso(RECURSO, pedido.getId().getStringValor())
                .enviar();
    }

    public void pedidoDecidido(PedidoAusencia pedido) {
        boolean aprovado = pedido.getEstado() != null && pedido.getEstado().isAprovado();
        String titulo = "O seu pedido de " + nomeDoTipo(pedido) + " " + Datas.periodo(pedido.getDataInicio(), pedido.getDataFim())
                + (aprovado ? " foi aprovado" : " foi rejeitado");
        notificador.para(pedido.getFuncionarioId())
                .tipo(TipoNotificacao.PEDIDO_AUSENCIA_DECIDIDO)
                .titulo(titulo)
                .texto(pedido.getObservacoesDecisao())
                .recurso(RECURSO, pedido.getId().getStringValor())
                .enviar();
    }

    private String nomeDoTipo(PedidoAusencia pedido) {
        return tipoAusenciaRepository.findById(pedido.getTipoAusenciaId())
                .map(TipoAusencia::getNome).map(String::toLowerCase).orElse("ausência");
    }
}
