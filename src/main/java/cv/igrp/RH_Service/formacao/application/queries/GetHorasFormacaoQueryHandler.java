package cv.igrp.RH_Service.formacao.application.queries;

import cv.igrp.RH_Service.formacao.application.dto.HorasFormacaoDTO;
import cv.igrp.RH_Service.formacao.application.services.FormacaoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetHorasFormacaoQueryHandler implements QueryHandler<GetHorasFormacaoQuery, ResponseEntity<List<HorasFormacaoDTO>>> {

    private final FormacaoService service;

    @IgrpQueryHandler
    public ResponseEntity<List<HorasFormacaoDTO>> handle(GetHorasFormacaoQuery q) {
        int ano = q.getAno() != null ? q.getAno() : java.time.LocalDate.now().getYear();
        return ResponseEntity.ok(service.horasNoAno(ano).entrySet().stream()
                .map(e -> new HorasFormacaoDTO(e.getKey().getStringValor(), service.nome(e.getKey()), e.getValue())).toList());
    }
}
