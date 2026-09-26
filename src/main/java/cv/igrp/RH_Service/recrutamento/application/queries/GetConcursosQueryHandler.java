package cv.igrp.RH_Service.recrutamento.application.queries;

import cv.igrp.RH_Service.recrutamento.application.dto.ConcursoDTO;
import cv.igrp.RH_Service.recrutamento.application.services.ConcursoService;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetConcursosQueryHandler implements QueryHandler<GetConcursosQuery, ResponseEntity<List<ConcursoDTO>>> {

    private final ConcursoService service;

    @IgrpQueryHandler
    public ResponseEntity<List<ConcursoDTO>> handle(GetConcursosQuery q) {
        return ResponseEntity.ok(service.listar(ConcursosDtos.valor(Concurso.Estado.class, q.getEstado(), "Estado"))
                .stream().map(c -> ConcursosDtos.dto(c, null)).toList());
    }
}
