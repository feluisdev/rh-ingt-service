package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.RH_Service.formacao.application.dto.PlanoFormacaoDTO;
import cv.igrp.RH_Service.formacao.domain.valueobject.PlanoFormacaoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPlanoFormacaoQueryHandler implements QueryHandler<GetPlanoFormacaoQuery, ResponseEntity<PlanoFormacaoDTO>> {

    private final FormacaoService service;
    private final FormacaoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<PlanoFormacaoDTO> handle(GetPlanoFormacaoQuery q) {
        return ResponseEntity.ok(dtos.dto(service.plano(PlanoFormacaoId.from(Entrada.uuid(q.getPlanoId(), "o plano")))));
    }
}
