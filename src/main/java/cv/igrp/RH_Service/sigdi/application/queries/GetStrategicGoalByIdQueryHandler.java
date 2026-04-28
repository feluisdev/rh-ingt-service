package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.StategicGoalResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.StrategicGoalRepository;
import cv.igrp.RH_Service.sigdi.domain.strategy.valueobject.StrategicGoalId;
import cv.igrp.RH_Service.sigdi.infrastructure.mappers.strategy.StrategicGoalMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
public class GetStrategicGoalByIdQueryHandler implements QueryHandler<GetStrategicGoalByIdQuery, ResponseEntity<StategicGoalResponseDTO>> {

    private final StrategicGoalRepository goalRepository;
    private final StrategicGoalMapper goalMapper;

    public GetStrategicGoalByIdQueryHandler(StrategicGoalRepository goalRepository, StrategicGoalMapper goalMapper) {
        this.goalRepository = goalRepository;
        this.goalMapper = goalMapper;
    }

    @IgrpQueryHandler
    public ResponseEntity<StategicGoalResponseDTO> handle(GetStrategicGoalByIdQuery query) {
        StrategicGoalId goalId = StrategicGoalId.from(UUID.fromString(query.getId()));
        
        return goalRepository.findById(goalId)
                .map(goalMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategic goal not found"));
    }
}
