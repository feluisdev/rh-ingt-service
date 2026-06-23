package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.domain.filter.GradeFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.GradeRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetGradesComboboxQueryHandler
        implements QueryHandler<GetGradesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final GradeRepository gradeRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetGradesComboboxQuery query) {
        var filter = new GradeFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        if (query.getCategoryId() != null) {
            filter.setCategoryId(java.util.UUID.fromString(query.getCategoryId()));
        }
        var items = gradeRepository.findAll(filter).getData().stream()
                .map(g -> new ComboboxItemDTO(g.getId().getStringValor(), g.getName()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
