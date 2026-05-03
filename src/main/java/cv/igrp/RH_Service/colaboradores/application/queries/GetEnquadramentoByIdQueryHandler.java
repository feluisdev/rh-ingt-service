package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.EnquadramentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.EnquadramentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetEnquadramentoByIdQueryHandler
        implements QueryHandler<GetEnquadramentoByIdQuery, ResponseEntity<EnquadramentoResponseDTO>> {

    private final EnquadramentoRepository enquadramentoRepository;
    private final EnquadramentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<EnquadramentoResponseDTO> handle(GetEnquadramentoByIdQuery query) {
        var e = enquadramentoRepository.findById(EnquadramentoId.from(query.getEnquadramentoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Enquadramento não encontrado: " + query.getEnquadramentoId()));
        return ResponseEntity.ok(mapper.toDTO(e));
    }
}
