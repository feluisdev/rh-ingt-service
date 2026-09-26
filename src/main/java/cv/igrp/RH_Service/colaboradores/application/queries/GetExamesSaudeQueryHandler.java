package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ExameSaudeDTO;
import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetExamesSaudeQueryHandler implements QueryHandler<GetExamesSaudeQuery, ResponseEntity<List<ExameSaudeDTO>>> {

    private final SaudeTrabalhoService service;
    private final SaudeTrabalhoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<List<ExameSaudeDTO>> handle(GetExamesSaudeQuery q) {
        var fid = FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador"));
        return ResponseEntity.ok(service.exames(fid).stream().map(e -> dtos.dto(e, null)).toList());
    }
}
