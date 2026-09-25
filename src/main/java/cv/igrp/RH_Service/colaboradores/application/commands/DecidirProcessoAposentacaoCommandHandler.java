package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DecisaoAposentacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.AposentacaoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoAposentacaoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecidirProcessoAposentacaoCommandHandler
        implements CommandHandler<DecidirProcessoAposentacaoCommand, ResponseEntity<ProcessoAposentacaoDTO>> {

    private final AposentacaoService aposentacaoService;

    @IgrpCommandHandler
    public ResponseEntity<ProcessoAposentacaoDTO> handle(DecidirProcessoAposentacaoCommand command) {
        var f = FuncionarioId.from(Entrada.uuid(command.getFuncionarioId(), "o colaborador"));
        var id = ProcessoAposentacaoId.from(Entrada.uuid(command.getProcessoId(), "o processo de aposentação"));
        DecisaoAposentacaoRequestDTO r = command.getRequest() != null ? command.getRequest() : new DecisaoAposentacaoRequestDTO();
        var p = switch (command.getAccao()) {
            case "DEFERIR" -> aposentacaoService.deferir(f, id, r.getDespachoNumero(), r.getDespachoData(), r.getDataPrevista());
            case "INDEFERIR" -> aposentacaoService.indeferir(f, id, r.getMotivo());
            case "DESLIGAR" -> aposentacaoService.desligar(f, id, r.getData(), r.getPercentagemPrestacao(), r.getWorkerStateId());
            case "CONCLUIR" -> aposentacaoService.concluir(f, id, r.getData(), r.getWorkerStateId(), r.getObservacao());
            case "CANCELAR" -> aposentacaoService.cancelar(f, id, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + command.getAccao());
        };
        return ResponseEntity.ok(AposentacaoDtos.dto(p));
    }
}
