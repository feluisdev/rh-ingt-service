package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ExoneracaoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ExoneracaoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExoneracaoId;
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
public class AccaoExoneracaoCommandHandler implements CommandHandler<AccaoExoneracaoCommand, ResponseEntity<ExoneracaoDTO>> {

    private final ExoneracaoService service;
    private final ExoneracaoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<ExoneracaoDTO> handle(AccaoExoneracaoCommand c) {
        ExoneracaoRequestDTO r = c.getRequest() != null ? c.getRequest() : new ExoneracaoRequestDTO();
        FuncionarioId fid = c.isComoMe() ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        if ("PEDIR".equals(c.getAccao())) {
            var res = service.pedir(fid, r.getDataPreAviso(), r.getDataPretendida(), r.getMotivo(), c.isComoMe());
            return ResponseEntity.status(201).body(dtos.dto(res.exoneracao(), res.condicionantes()));
        }
        var id = ExoneracaoId.from(Entrada.uuid(c.getExoneracaoId(), "o pedido de exoneração"));
        var res = switch (c.getAccao()) {
            case "DEFERIR" -> service.deferir(fid, id, r.getDespacho(), r.getData());
            case "DESISTIR" -> service.desistir(fid, id);
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(dtos.dto(res.exoneracao(), null));
    }
}
