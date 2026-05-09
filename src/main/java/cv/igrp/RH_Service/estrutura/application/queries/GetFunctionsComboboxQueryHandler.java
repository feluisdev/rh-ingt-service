package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.domain.filter.FunctionFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.FunctionRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetFunctionsComboboxQueryHandler
        implements QueryHandler<GetFunctionsComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final FunctionRepository functionRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetFunctionsComboboxQuery query) {
        var filter = new FunctionFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = functionRepository.findAll(filter).getData().stream()
                .map(f -> new ComboboxItemDTO(f.getId().getStringValor(), f.getName()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
