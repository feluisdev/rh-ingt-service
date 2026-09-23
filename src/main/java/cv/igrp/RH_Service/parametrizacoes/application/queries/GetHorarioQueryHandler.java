package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetHorarioQueryHandler implements QueryHandler<GetHorarioQuery, ResponseEntity<HorarioResponseDTO>> {

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;

    @IgrpQueryHandler
    public ResponseEntity<HorarioResponseDTO> handle(GetHorarioQuery query) {
        return horarioRepository.findById(HorarioId.from(query.getHorarioId()))
                .map(horarioMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Horário não encontrado: " + query.getHorarioId()));
    }
}
