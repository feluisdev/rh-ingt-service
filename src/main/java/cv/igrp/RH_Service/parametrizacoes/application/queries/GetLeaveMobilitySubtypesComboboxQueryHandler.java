package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.LeaveMobilitySubtypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetLeaveMobilitySubtypesComboboxQueryHandler
        implements QueryHandler<GetLeaveMobilitySubtypesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetLeaveMobilitySubtypesComboboxQuery query) {
        var filter = new LeaveMobilitySubtypeFilter();
        filter.setActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = leaveMobilitySubtypeRepository.findAll(filter).getData().stream()
                .map(lms -> new ComboboxItemDTO(lms.getId().getStringValor(), lms.getDescription()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
