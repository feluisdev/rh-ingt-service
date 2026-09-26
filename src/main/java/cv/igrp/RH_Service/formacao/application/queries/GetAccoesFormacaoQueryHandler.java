package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.RH_Service.formacao.application.dto.AccaoFormacaoDTO;
import cv.igrp.RH_Service.formacao.domain.models.AccaoFormacao;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAccoesFormacaoQueryHandler implements QueryHandler<GetAccoesFormacaoQuery, ResponseEntity<List<AccaoFormacaoDTO>>> {

    private final FormacaoService service;
    private final FormacaoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<AccaoFormacaoDTO>> handle(GetAccoesFormacaoQuery q) {
        if (q.isComoMe()) {
            var eu = currentEmployeeResolver.resolve();
            return ResponseEntity.ok(service.paraMim(eu).stream().map(a -> dtos.dto(a, eu)).toList());
        }
        return ResponseEntity.ok(service.accoes(FormacaoDtos.valor(AccaoFormacao.Estado.class, q.getEstado(), "Estado"), q.getAno())
                .stream().map(a -> dtos.dto(a, null)).toList());
    }
}
