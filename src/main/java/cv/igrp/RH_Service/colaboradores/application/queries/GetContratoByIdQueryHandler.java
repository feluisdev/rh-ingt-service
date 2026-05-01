package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ContratoResponse;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ContratoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetContratoByIdQueryHandler")
@RequiredArgsConstructor
public class GetContratoByIdQueryHandler
        implements QueryHandler<GetContratoByIdQuery, ResponseEntity<ContratoResponse>> {

    private final ContratoRepository contratoRepository;
    private final ContratoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<ContratoResponse> handle(GetContratoByIdQuery query) {
        var c = contratoRepository.findById(ContratoId.from(query.getContratoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + query.getContratoId()));
        return ResponseEntity.ok(mapper.toDTO(c));
    }
}
