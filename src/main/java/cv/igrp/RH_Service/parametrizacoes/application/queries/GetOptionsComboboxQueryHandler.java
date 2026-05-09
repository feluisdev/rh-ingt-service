package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.OptionFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetOptionsComboboxQueryHandler
        implements QueryHandler<GetOptionsComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final OptionRepository optionRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetOptionsComboboxQuery query) {
        var filter = new OptionFilter();
        filter.setCcode(query.getCcode());
        filter.setActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = optionRepository.findAll(filter).getData().stream()
                .map(opt -> new ComboboxItemDTO(opt.getId().getStringValor(), opt.getCvalue()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
