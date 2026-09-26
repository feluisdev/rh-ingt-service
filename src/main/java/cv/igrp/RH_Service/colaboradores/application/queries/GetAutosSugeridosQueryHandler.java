package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AutoSugeridoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AutosAssiduidadeService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetAutosSugeridosQueryHandler implements QueryHandler<GetAutosSugeridosQuery, ResponseEntity<List<AutoSugeridoDTO>>> {

    private final AutosAssiduidadeService service;

    @IgrpQueryHandler
    public ResponseEntity<List<AutoSugeridoDTO>> handle(GetAutosSugeridosQuery q) {
        return ResponseEntity.ok(service.sugestoes(LocalDate.now()).stream().map(s -> new AutoSugeridoDTO(s.funcionarioId().getStringValor(),
                s.nome(), s.sinal().especie().name(), s.sinal().motivo(), s.sinal().seguidos(), s.sinal().noAno(), s.sinal().em24Meses())).toList());
    }
}
