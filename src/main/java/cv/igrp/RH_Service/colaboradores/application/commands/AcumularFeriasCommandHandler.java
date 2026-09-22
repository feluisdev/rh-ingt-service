package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFeriasResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.FeriasService;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * <b>Acumular férias para o ano seguinte</b> — DL n.º 3/2010, art. 7.º n.º 1.
 *
 * <p>Depois de o saldo passar a nascer sozinho (V49), o saldo do ano novo aparecia a 1 de Janeiro
 * com o direito inteiro e os dias não gozados do ano anterior ficavam na linha antiga, sem
 * caminho: um pedido de 2027 só olha para o saldo de 2027. Não se perdiam da base — perdiam-se na
 * prática, e o art. 2.º n.º 5 diz que o direito é irrenunciável.
 *
 * <p>Este é um <b>acto do RH, justificado</b>, e não um automatismo: a lei só permite a
 * acumulação quando, «por motivo de serviço», as férias não puderam ser gozadas nesse ano.
 */
@Component("colabsAcumularFeriasCommandHandler")
@RequiredArgsConstructor
public class AcumularFeriasCommandHandler
        implements CommandHandler<AcumularFeriasCommand, ResponseEntity<AcumulacaoFeriasResponseDTO>> {

    private final SaldoAusenciaRepository saldoRepository;
    private final FeriasService feriasService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<AcumulacaoFeriasResponseDTO> handle(AcumularFeriasCommand command) {
        if (command.getDias() == null)
            throw IgrpResponseStatusException.badRequest("O número de dias a acumular é obrigatório.");

        var origem = saldoRepository.findById(SaldoAusenciaId.from(command.getSaldoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Saldo não encontrado: " + command.getSaldoId()));

        // O saldo tem de ser de quem o URL diz: sem isto, o id de um saldo de outra pessoa passava.
        if (!origem.getFuncionarioId().equals(FuncionarioId.from(command.getFuncionarioId())))
            throw IgrpResponseStatusException.notFound(
                    "Saldo não encontrado para este colaborador: " + command.getSaldoId());

        var destino = feriasService.acumularParaOAnoSeguinte(origem, command.getDias(), command.getMotivo());

        return ResponseEntity.ok(new AcumulacaoFeriasResponseDTO(
                destino.getId().getStringValor(),
                origem.getAno(),
                destino.getAno(),
                command.getDias(),
                origem.saldoDisponivel(),
                destino.saldoDisponivel()));
    }
}
