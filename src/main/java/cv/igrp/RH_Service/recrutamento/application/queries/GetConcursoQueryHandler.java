package cv.igrp.RH_Service.recrutamento.application.queries;

import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.services.ConcursoService;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetConcursoQueryHandler implements QueryHandler<GetConcursoQuery, ResponseEntity<ConcursoDTO>> {

    private final ConcursoService service;

    @IgrpQueryHandler
    public ResponseEntity<ConcursoDTO> handle(GetConcursoQuery q) {
        var id = ConcursoId.from(Entrada.uuid(q.getConcursoId(), "o concurso"));
        return ResponseEntity.ok(ConcursosDtos.dto(service.concurso(id), service.candidaturas(id)));
    }
}
