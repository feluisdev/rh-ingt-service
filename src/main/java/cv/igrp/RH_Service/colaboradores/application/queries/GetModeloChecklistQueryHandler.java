package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistModeloDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetModeloChecklistQueryHandler implements QueryHandler<GetModeloChecklistQuery, ResponseEntity<List<ItemChecklistModeloDTO>>> {

    private final ChecklistService service;

    @IgrpQueryHandler
    public ResponseEntity<List<ItemChecklistModeloDTO>> handle(GetModeloChecklistQuery q) {
        return ResponseEntity.ok(service.modelo(ChecklistDtos.valor(TipoChecklist.class, q.getTipo(), "Tipo de checklist"))
                .stream().map(ChecklistDtos::dto).toList());
    }
}
