package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoDTO;
import cv.igrp.RH_Service.formacao.domain.valueobject.AccaoFormacaoId;
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
public class GetAccaoFormacaoQueryHandler implements QueryHandler<GetAccaoFormacaoQuery, ResponseEntity<AccaoFormacaoDTO>> {

    private final FormacaoService service;
    private final FormacaoDtos dtos;

    @IgrpQueryHandler
    public ResponseEntity<AccaoFormacaoDTO> handle(GetAccaoFormacaoQuery q) {
        return ResponseEntity.ok(dtos.dto(service.accao(AccaoFormacaoId.from(Entrada.uuid(q.getAccaoId(), "a acção"))), null));
    }
}
