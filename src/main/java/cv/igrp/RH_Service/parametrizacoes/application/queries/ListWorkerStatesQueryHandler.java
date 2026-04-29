package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaWorkerStateDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.WorkerStateFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.WorkerStateMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ListWorkerStatesQueryHandler implements QueryHandler<ListWorkerStatesQuery, ResponseEntity<WrapperListaWorkerStateDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListWorkerStatesQueryHandler.class);

    private final WorkerStateRepository workerStateRepository;
    private final WorkerStateMapper workerStateMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaWorkerStateDTO> handle(ListWorkerStatesQuery query) {
        var filter = new WorkerStateFilter();
        filter.setCode(query.getCode());
        filter.setIsActive(query.getIsActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        var content = workerStateRepository.findAll(filter).stream()
            .map(workerStateMapper::toDTO)
            .toList();

        var wrapper = new WrapperListaWorkerStateDTO();
        wrapper.setContent(new ArrayList<>(content));
        wrapper.setTotalElements((long) content.size());

        return ResponseEntity.ok(wrapper);
    }
}
