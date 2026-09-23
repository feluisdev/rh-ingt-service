package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.services.ParametrosFeriasService;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ParametroFeriasMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Os parâmetros que valem no ano (o corrente, se não vier): da tabela, ou os da lei. */
@Component
@RequiredArgsConstructor
public class GetParametroFeriasVigenteQueryHandler implements QueryHandler<GetParametroFeriasVigenteQuery, ResponseEntity<ParametroFeriasResponseDTO>> {

    private final ParametrosFeriasService parametrosFeriasService;
    private final ParametroFeriasMapper parametroFeriasMapper;

    @IgrpQueryHandler
    public ResponseEntity<ParametroFeriasResponseDTO> handle(GetParametroFeriasVigenteQuery query) {
        int ano = query.getAno() != null ? query.getAno() : LocalDate.now().getYear();
        return ResponseEntity.ok(parametroFeriasMapper.toDTO(parametrosFeriasService.vigenteEm(ano)));
    }
}
