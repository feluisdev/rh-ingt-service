package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.TramitacaoDisciplinarDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ProcessoDisciplinarService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTramitacaoDisciplinarQueryHandler implements QueryHandler<GetTramitacaoDisciplinarQuery, ResponseEntity<TramitacaoDisciplinarDTO>> {

    private final ProcessoDisciplinarService service;
    private final TramitacaoDisciplinarDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<TramitacaoDisciplinarDTO> handle(GetTramitacaoDisciplinarQuery q) {
        var fid = FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador"));
        var id = ProcessoDisciplinarId.from(Entrada.uuid(q.getProcessoId(), "o processo"));
        return ResponseEntity.ok(dtos.dto(service.ver(fid, id), null));
    }
}
