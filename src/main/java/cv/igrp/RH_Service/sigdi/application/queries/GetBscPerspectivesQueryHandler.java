package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.sigdi.application.dto.BscPerspectiveItemDTO;
import cv.igrp.RH_Service.sigdi.domain.strategy.repository.BscPerspectiveConfigRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Reads all 4 configured BSC perspectives, sorted ascending by display order
 * (see {@link BscPerspectiveConfigRepository#findAll()}). No {@code @Cacheable} -- volume is
 * trivial (4 rows) and CONTEXT.md explicitly calls for "sem cache".
 */
@Component
@RequiredArgsConstructor
public class GetBscPerspectivesQueryHandler
        implements QueryHandler<GetBscPerspectivesQuery, ResponseEntity<List<BscPerspectiveItemDTO>>> {

    private final BscPerspectiveConfigRepository repository;

    @IgrpQueryHandler
    public ResponseEntity<List<BscPerspectiveItemDTO>> handle(GetBscPerspectivesQuery query) {
        List<BscPerspectiveItemDTO> items = repository.findAll().stream()
                .map(c -> new BscPerspectiveItemDTO(c.getCode(), c.getLabel(), c.getDisplayOrder()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
