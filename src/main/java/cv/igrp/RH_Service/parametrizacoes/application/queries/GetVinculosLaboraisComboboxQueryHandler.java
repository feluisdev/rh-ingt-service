package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.VinculoLaboralFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.VinculoLaboralRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetVinculosLaboraisComboboxQueryHandler
        implements QueryHandler<GetVinculosLaboraisComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final VinculoLaboralRepository vinculoLaboralRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetVinculosLaboraisComboboxQuery query) {
        var filter = new VinculoLaboralFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = vinculoLaboralRepository.findAll(filter).getData().stream()
                .map(vl -> new ComboboxItemDTO(vl.getId().getStringValor(), vl.getDescription()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
