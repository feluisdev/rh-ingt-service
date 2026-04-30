package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WorkerStateResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.WorkerStateMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetWorkerStateQueryHandler implements QueryHandler<GetWorkerStateQuery, ResponseEntity<WorkerStateResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetWorkerStateQueryHandler.class);

    private final WorkerStateRepository workerStateRepository;
    private final WorkerStateMapper workerStateMapper;

    @IgrpQueryHandler
    public ResponseEntity<WorkerStateResponseDTO> handle(GetWorkerStateQuery query) {
        var id = WorkerStateId.from(UUID.fromString(query.getWorkerStateId()));

        return workerStateRepository.findById(id)
            .map(workerStateMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + query.getWorkerStateId()));
    }
}
