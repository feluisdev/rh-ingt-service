package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.SuspensaoFeriasResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CalendarioFeriadosService;
import cv.igrp.RH_Service.colaboradores.application.services.SaldoAusenciaService;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.PedidoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.service.DiasUteisCalculator;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * <b>Suspender férias</b> — DL n.º 3/2010, art. 8.º.
 *
 * <p>As férias suspendem-se por maternidade, paternidade ou adopção (n.º 1), por doença e
 * assistência inadiável a familiares doentes (n.º 2) e, por razões imperiosas de serviço, por
 * despacho fundamentado do dirigente (n.º 5). O n.º 3 diz a partir de quando: «a partir da data
 * da entrada no serviço do documento comprovativo».
 *
 * <p>Até aqui um pedido de férias e um de doença não se falavam: quem adoecesse a meio das
 * férias perdia-as, porque os dias já tinham sido contados como gozados na aprovação.
 *
 * <p><b>Os dias recuperados voltam ao saldo do próprio ano</b>, e é tudo o que este caminho faz.
 * Passá-los ao ano seguinte é a acumulação (V50) — e é exactamente isso que o art. 9.º n.º 1, ao
 * remeter para o art. 8.º n.º 4, autoriza ao mandar gozá-los «até ao termo do ano civil imediato».
 * Foi por isso que não se inventou aqui um segundo mecanismo de transporte.
 *
 * <p>Os dias contam-se em <b>dias úteis</b>, com o mesmo calculador da submissão: recontam-se os
 * do período encurtado e devolve-se a diferença. Recontar em vez de subtrair à mão evita que a
 * contagem da suspensão e a da criação divirjam.
 */
@Component("colabsSuspenderFeriasCommandHandler")
@RequiredArgsConstructor
public class SuspenderFeriasCommandHandler
        implements CommandHandler<SuspenderFeriasCommand, ResponseEntity<SuspensaoFeriasResponseDTO>> {

    private final PedidoAusenciaRepository pedidoRepository;
    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final SaldoAusenciaRepository saldoRepository;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final DiasUteisCalculator diasUteisCalculator;
    private final SaldoAusenciaService saldoAusenciaService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuspensaoFeriasResponseDTO> handle(SuspenderFeriasCommand command) {
        var pedido = pedidoRepository.findById(PedidoAusenciaId.from(command.getPedidoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Pedido de ausência não encontrado: " + command.getPedidoId()));

        // O pedido tem de ser de quem o URL diz: sem isto, o id de um pedido de outra pessoa passava.
        if (!pedido.getFuncionarioId().equals(FuncionarioId.from(command.getFuncionarioId())))
            throw IgrpResponseStatusException.notFound(
                    "Pedido de ausência não encontrado para este colaborador: " + command.getPedidoId());

        var tipo = tipoAusenciaRepository.findById(pedido.getTipoAusenciaId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Tipo de ausência não encontrado: " + pedido.getTipoAusenciaId().getStringValor()));

        if (!tipo.isFerias())
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A suspensão é das férias (DL n.º 3/2010, art. 8.º). O tipo '" + tipo.getNome()
                            + "' está classificado como " + tipo.getRegime() + ".");

        int diasAntes = pedido.getNumeroDias();
        LocalDate inicio = pedido.getDataInicio();

        pedido.suspender(command.getData(), command.getMotivo(), LocalDate.now());

        int diasGozados = diasUteisCalculator.calcular(inicio, pedido.getDataFim(),
                calendarioFeriadosService.feriadosDoColaborador(pedido.getFuncionarioId(), inicio, pedido.getDataFim()));
        int diasRecuperados = Math.max(0, diasAntes - diasGozados);

        pedido.ajustarNumeroDias(diasGozados);
        pedidoRepository.save(pedido);

        // Os dias que deixaram de ser gozados voltam ao saldo. O tipo pode não descontar saldo --
        // aí não há nada a devolver, e o serviço trata disso.
        if (diasRecuperados > 0)
            saldoAusenciaService.devolverDias(pedido, diasRecuperados);

        Integer disponivel = saldoRepository
                .findByFuncionarioIdAndTipoAusenciaIdAndAno(
                        pedido.getFuncionarioId(), pedido.getTipoAusenciaId(), inicio.getYear())
                .map(s -> s.saldoDisponivel())
                .orElse(null);

        return ResponseEntity.ok(new SuspensaoFeriasResponseDTO(
                pedido.getId().getStringValor(),
                pedido.getSuspensoEm(),
                pedido.getDataFim(),
                diasGozados,
                diasRecuperados,
                disponivel));
    }
}
