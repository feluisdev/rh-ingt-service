package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ComissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ComissaoServicoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetComissoesServicoQueryHandler implements QueryHandler<GetComissoesServicoQuery, ResponseEntity<List<ComissaoServicoDTO>>> {

    private final ComissaoServicoService service;
    private final ComissaoServicoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<List<ComissaoServicoDTO>> handle(GetComissoesServicoQuery q) {
        return ResponseEntity.ok(service.emCurso(q.getTerminaAte()).stream().map(dtos::dto).toList());
    }
}
