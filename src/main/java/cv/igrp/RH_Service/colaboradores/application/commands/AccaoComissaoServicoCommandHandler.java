package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.queries.ComissaoServicoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ComissaoServicoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoComissaoServicoCommandHandler implements CommandHandler<AccaoComissaoServicoCommand, ResponseEntity<ComissaoServicoDTO>> {

    private final ComissaoServicoService service;
    private final ComissaoServicoDtos dtos;

    @IgrpCommandHandler
    public ResponseEntity<ComissaoServicoDTO> handle(AccaoComissaoServicoCommand c) {
        var fid = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        var id = LicencaMobilidadeId.from(Entrada.uuid(c.getLicencaId(), "a comissão de serviço").toString());
        ComissaoServicoRequestDTO r = c.getRequest() != null ? c.getRequest() : new ComissaoServicoRequestDTO();
        var comissao = switch (c.getAccao()) {
            case "RENOVAR" -> service.renovar(fid, id, r.getDespacho());
            case "CESSAR" -> service.cessar(fid, id, ChecklistDtos.valor(ComissaoServicoService.Iniciativa.class, r.getIniciativa(), "Iniciativa"),
                    r.getDataAviso(), r.getDataEfeito(), r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(comissao));
    }
}
