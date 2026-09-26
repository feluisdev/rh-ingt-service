package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FechoMensalDTO;
import cv.igrp.RH_Service.colaboradores.application.services.FechoMensalService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetFechosMensaisQueryHandler implements QueryHandler<GetFechosMensaisQuery, ResponseEntity<List<FechoMensalDTO>>> {

    private final FechoMensalService service;

    @IgrpQueryHandler
    public ResponseEntity<List<FechoMensalDTO>> handle(GetFechosMensaisQuery q) {
        return ResponseEntity.ok(service.fechos().stream().map(f -> FechoMensalDtos.dto(f, 0, null)).toList());
    }
}
