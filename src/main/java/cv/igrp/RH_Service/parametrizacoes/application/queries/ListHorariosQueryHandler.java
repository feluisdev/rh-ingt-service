package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** Os horários da instituição, por nome. Poucas linhas: sem paginação. */
@Component
@RequiredArgsConstructor
public class ListHorariosQueryHandler implements QueryHandler<ListHorariosQuery, ResponseEntity<List<HorarioResponseDTO>>> {

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;
    private final cv.igrp.RH_Service.parametrizacoes.application.services.HorarioBaseService horarioBaseService;

    @IgrpQueryHandler
    public ResponseEntity<List<HorarioResponseDTO>> handle(ListHorariosQuery query) {
        return ResponseEntity.ok(horarioRepository.findAll(query.getIsActive()).stream()
                .map(horarioMapper::toDTO).map(horarioBaseService::comBaseDeHoje).toList());
    }
}
