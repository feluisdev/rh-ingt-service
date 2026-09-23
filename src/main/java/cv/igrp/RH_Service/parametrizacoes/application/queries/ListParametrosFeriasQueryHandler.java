package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ParametroFeriasResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ParametroFeriasRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ParametroFeriasMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** As vigências da tabela, da mais antiga para a mais recente. Poucas linhas: sem paginação. */
@Component
@RequiredArgsConstructor
public class ListParametrosFeriasQueryHandler implements QueryHandler<ListParametrosFeriasQuery, ResponseEntity<List<ParametroFeriasResponseDTO>>> {

    private final ParametroFeriasRepository parametroFeriasRepository;
    private final ParametroFeriasMapper parametroFeriasMapper;

    @IgrpQueryHandler
    public ResponseEntity<List<ParametroFeriasResponseDTO>> handle(ListParametrosFeriasQuery query) {
        return ResponseEntity.ok(parametroFeriasRepository.findAll().stream()
                .map(parametroFeriasMapper::toDTO).toList());
    }
}
