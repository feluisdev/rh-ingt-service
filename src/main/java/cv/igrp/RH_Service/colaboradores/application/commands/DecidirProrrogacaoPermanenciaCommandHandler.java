package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.AposentacaoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProrrogacaoPermanenciaId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DecidirProrrogacaoPermanenciaCommandHandler
        implements CommandHandler<DecidirProrrogacaoPermanenciaCommand, ResponseEntity<ProrrogacaoPermanenciaDTO>> {

    private final AposentacaoService aposentacaoService;

    @IgrpCommandHandler
    public ResponseEntity<ProrrogacaoPermanenciaDTO> handle(DecidirProrrogacaoPermanenciaCommand command) {
        var f = FuncionarioId.from(Entrada.uuid(command.getFuncionarioId(), "o colaborador"));
        var id = ProrrogacaoPermanenciaId.from(Entrada.uuid(command.getProrrogacaoId(), "a prorrogação"));
        var r = command.getRequest() != null ? command.getRequest() : new ProrrogacaoPermanenciaRequestDTO();
        var p = command.isAutorizar()
                ? aposentacaoService.autorizarProrrogacao(f, id, r.getDespachoNumero(), r.getDespachoData())
                : aposentacaoService.indeferirProrrogacao(f, id, r.getMotivo());
        return ResponseEntity.ok(AposentacaoDtos.dto(p));
    }
}
