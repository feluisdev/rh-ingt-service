package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetPositionByIdQueryHandler
        implements QueryHandler<GetPositionByIdQuery, ResponseEntity<PositionResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPositionByIdQueryHandler.class);

    private final PositionRepository positionRepository;
    private final PositionMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<PositionResponseDTO> handle(GetPositionByIdQuery query) {
        var position = positionRepository.findById(PositionId.from(query.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + query.getPositionId()));

        return ResponseEntity.ok(mapper.toDTO(position));
    }
}
