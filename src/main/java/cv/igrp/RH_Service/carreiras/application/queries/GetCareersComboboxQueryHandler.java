package cv.igrp.RH_Service.carreiras.application.queries;

import cv.igrp.RH_Service.carreiras.domain.filter.CareerFilter;
import cv.igrp.RH_Service.carreiras.domain.repository.CareerRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetCareersComboboxQueryHandler
        implements QueryHandler<GetCareersComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final CareerRepository careerRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetCareersComboboxQuery query) {
        var filter = new CareerFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = careerRepository.findAll(filter).getData().stream()
                .map(c -> new ComboboxItemDTO(c.getId().getStringValor(), c.getName()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
