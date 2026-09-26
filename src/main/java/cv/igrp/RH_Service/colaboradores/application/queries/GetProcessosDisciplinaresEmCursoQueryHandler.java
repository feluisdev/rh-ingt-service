package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.TramitacaoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ProcessoDisciplinarService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetProcessosDisciplinaresEmCursoQueryHandler
        implements QueryHandler<GetProcessosDisciplinaresEmCursoQuery, ResponseEntity<List<TramitacaoDisciplinarDTO>>> {

    private final ProcessoDisciplinarService service;
    private final TramitacaoDisciplinarDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<List<TramitacaoDisciplinarDTO>> handle(GetProcessosDisciplinaresEmCursoQuery q) {
        return ResponseEntity.ok(service.emCurso().stream().map(p -> dtos.dto(p, null)).toList());
    }
}
