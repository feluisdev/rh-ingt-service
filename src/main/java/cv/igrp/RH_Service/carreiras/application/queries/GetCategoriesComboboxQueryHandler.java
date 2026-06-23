package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.domain.filter.CategoryFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.CategoryRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCategoriesComboboxQueryHandler
        implements QueryHandler<GetCategoriesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final CategoryRepository categoryRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetCategoriesComboboxQuery query) {
        var filter = new CategoryFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        if (query.getCareerId() != null) {
            filter.setCareerId(java.util.UUID.fromString(query.getCareerId()));
        }
        var items = categoryRepository.findAll(filter).getData().stream()
                .map(cat -> new ComboboxItemDTO(cat.getId().getStringValor(), cat.getName()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
