package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.FechoMensalDtos;
import cv.igrp.RH_Service.colaboradores.application.services.FechoMensalService;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoFechoMensalCommandHandler implements CommandHandler<AccaoFechoMensalCommand, ResponseEntity<FechoMensalDTO>> {

    private final FechoMensalService service;

    @IgrpCommandHandler
    public ResponseEntity<FechoMensalDTO> handle(AccaoFechoMensalCommand c) {
        var mes = FechoMensalDtos.mes(c.getMes());
        FechoMensalRequestDTO r = c.getRequest() != null ? c.getRequest() : new FechoMensalRequestDTO();
        var res = switch (c.getAccao()) {
            case "FECHAR" -> service.fechar(mes);
            case "REABRIR" -> service.reabrir(mes, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(FechoMensalDtos.dto(res.fecho(), res.linhas(), res.alertas()));
    }
}
