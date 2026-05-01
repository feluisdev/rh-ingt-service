package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.TipoAusenciaResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.TipoAusenciaMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetTipoAusenciaByIdQueryHandler")
@RequiredArgsConstructor
public class GetTipoAusenciaByIdQueryHandler
        implements QueryHandler<GetTipoAusenciaByIdQuery, ResponseEntity<TipoAusenciaResponse>> {

    private final TipoAusenciaRepository tipoAusenciaRepository;
    private final TipoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<TipoAusenciaResponse> handle(GetTipoAusenciaByIdQuery query) {
        var tipo = tipoAusenciaRepository.findById(TipoAusenciaId.from(query.getId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Tipo de ausência não encontrado: " + query.getId()));
        return ResponseEntity.ok(mapper.toDTO(tipo));
    }
}
