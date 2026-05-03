package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriadoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriadoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FeriadoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetFeriadoByIdQueryHandler")
@RequiredArgsConstructor
public class GetFeriadoByIdQueryHandler
        implements QueryHandler<GetFeriadoByIdQuery, ResponseEntity<FeriadoResponseDTO>> {

    private final FeriadoRepository feriadoRepository;
    private final FeriadoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<FeriadoResponseDTO> handle(GetFeriadoByIdQuery query) {
        var feriado = feriadoRepository.findById(FeriadoId.from(query.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Feriado não encontrado: " + query.getId()));
        return ResponseEntity.ok(mapper.toDTO(feriado));
    }
}
