package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.WorkerStateFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetWorkerStatesComboboxQueryHandler
        implements QueryHandler<GetWorkerStatesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final WorkerStateRepository workerStateRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetWorkerStatesComboboxQuery query) {
        var filter = new WorkerStateFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = workerStateRepository.findAll(filter).getData().stream()
                .map(ws -> new ComboboxItemDTO(ws.getId().getStringValor(), ws.getDescription()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
