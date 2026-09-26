package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetChecklistsPendentesQueryHandler implements QueryHandler<GetChecklistsPendentesQuery, ResponseEntity<List<ChecklistDTO>>> {

    private final ChecklistService service;

    @IgrpQueryHandler
    public ResponseEntity<List<ChecklistDTO>> handle(GetChecklistsPendentesQuery q) {
        var hoje = LocalDate.now();
        return ResponseEntity.ok(service.pendentes(ChecklistDtos.valor(TipoChecklist.class, q.getTipo(), "Tipo de checklist"),
                        ChecklistDtos.valor(ResponsavelChecklist.class, q.getResponsavel(), "Responsável"), Boolean.TRUE.equals(q.getAtrasadas()))
                .stream().map(c -> ChecklistDtos.dto(c, service.nomeDe(c.getFuncionarioId()), hoje)).toList());
    }
}
