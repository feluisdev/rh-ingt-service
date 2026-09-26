package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.AcumulacaoFuncoesDtos;
import cv.igrp.RH_Service.colaboradores.application.services.AcumulacaoFuncoesService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcumulacaoFuncoesId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoAcumulacaoFuncoesCommandHandler implements CommandHandler<AccaoAcumulacaoFuncoesCommand, ResponseEntity<AcumulacaoFuncoesDTO>> {

    private final AcumulacaoFuncoesService service;
    private final AcumulacaoFuncoesDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<AcumulacaoFuncoesDTO> handle(AccaoAcumulacaoFuncoesCommand c) {
        AcumulacaoFuncoesRequestDTO r = c.getRequest() != null ? c.getRequest() : new AcumulacaoFuncoesRequestDTO();
        FuncionarioId fid = c.isComoMe() ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        if ("PEDIR".equals(c.getAccao()))
            return ResponseEntity.status(201).body(dtos.dto(service.pedir(fid, AcumulacaoFuncoesDtos.dados(r), c.isComoMe())));
        var id = AcumulacaoFuncoesId.from(Entrada.uuid(c.getAcumulacaoId(), "a acumulação"));
        var a = switch (c.getAccao()) {
            case "AUTORIZAR" -> service.autorizar(fid, id, r.getDespacho(), r.getData());
            case "INDEFERIR" -> service.indeferir(fid, id, r.getMotivo(), r.getData());
            case "CESSAR" -> service.cessar(fid, id, r.getData(), r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(a));
    }
}
