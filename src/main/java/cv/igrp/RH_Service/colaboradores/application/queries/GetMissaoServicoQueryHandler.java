package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.MissaoServicoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MissaoServicoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetMissaoServicoQueryHandler implements QueryHandler<GetMissaoServicoQuery, ResponseEntity<MissaoServicoDTO>> {

    private final MissaoServicoService service;
    private final MissaoServicoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<MissaoServicoDTO> handle(GetMissaoServicoQuery q) {
        var m = service.missao(MissaoServicoId.from(Entrada.uuid(q.getMissaoId(), "a missão")));
        return ResponseEntity.ok(dtos.dto(m, service.alertas(m)));
    }
}
