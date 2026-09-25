package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.AposentacaoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PedirProrrogacaoPermanenciaCommandHandler
        implements CommandHandler<PedirProrrogacaoPermanenciaCommand, ResponseEntity<ProrrogacaoPermanenciaDTO>> {

    private final AposentacaoService aposentacaoService;

    @IgrpCommandHandler
    public ResponseEntity<ProrrogacaoPermanenciaDTO> handle(PedirProrrogacaoPermanenciaCommand command) {
        var r = Entrada.corpo(command.getRequest(), "a manifestação de vontade, a proposta fundamentada e a data");
        var f = FuncionarioId.from(Entrada.uuid(command.getFuncionarioId(), "o colaborador"));
        var p = aposentacaoService.pedirProrrogacao(f, Boolean.TRUE.equals(r.getManifestacaoVontade()),
                r.getPropostaFundamentada(), r.getValidaAte());
        return ResponseEntity.status(201).body(AposentacaoDtos.dto(p));
    }
}
