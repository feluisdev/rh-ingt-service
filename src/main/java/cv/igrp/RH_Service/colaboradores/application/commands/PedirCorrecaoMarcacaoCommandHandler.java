package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssiduidadeService;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/** Pedido de correcção do próprio: fica PENDENTE, e só conta validado pela chefia directa ou pelo RH. */
@Component
@RequiredArgsConstructor
public class PedirCorrecaoMarcacaoCommandHandler implements CommandHandler<PedirCorrecaoMarcacaoCommand, ResponseEntity<SuccessResponseDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final AssiduidadeService assiduidadeService;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(PedirCorrecaoMarcacaoCommand command) {
        var dto = command.getRequest();
        var m = assiduidadeService.pedirCorrecao(currentEmployeeResolver.resolve(), dto.getMomento(),
                LancarMarcacaoCommandHandler.sentido(dto.getSentido()), dto.getMotivo());
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(m.getId().getStringValor()));
    }
}
