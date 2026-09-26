package cv.igrp.RH_Service.recrutamento.application.queries;

import cv.igrp.RH_Service.recrutamento.application.dto.CandidaturaDTO;
import cv.igrp.RH_Service.recrutamento.application.services.ConcursoService;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetMinhasCandidaturasQueryHandler implements QueryHandler<GetMinhasCandidaturasQuery, ResponseEntity<List<CandidaturaDTO>>> {

    private final ConcursoService service;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<CandidaturaDTO>> handle(GetMinhasCandidaturasQuery q) {
        return ResponseEntity.ok(service.doFuncionario(currentEmployeeResolver.resolve().getValor()).stream()
                .map(ConcursosDtos::dto).toList());
    }
}
