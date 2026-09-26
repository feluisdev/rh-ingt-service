package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPlanosFormacaoQueryHandler implements QueryHandler<GetPlanosFormacaoQuery, ResponseEntity<List<PlanoFormacaoDTO>>> {

    private final FormacaoService service;
    private final FormacaoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<List<PlanoFormacaoDTO>> handle(GetPlanosFormacaoQuery q) {
        return ResponseEntity.ok(service.planos(q.getAno()).stream().map(dtos::dto).toList());
    }
}
