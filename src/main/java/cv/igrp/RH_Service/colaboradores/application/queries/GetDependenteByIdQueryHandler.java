package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DependenteResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DependenteRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DependenteMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetDependenteByIdQueryHandler")
@RequiredArgsConstructor
public class GetDependenteByIdQueryHandler
        implements QueryHandler<GetDependenteByIdQuery, ResponseEntity<DependenteResponseDTO>> {

    private final DependenteRepository dependenteRepository;
    private final DependenteMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<DependenteResponseDTO> handle(GetDependenteByIdQuery query) {
        var d = dependenteRepository.findById(DependenteId.from(query.getDependenteId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Dependente não encontrado: " + query.getDependenteId()));
        return ResponseEntity.ok(mapper.toDTO(d));
    }
}
