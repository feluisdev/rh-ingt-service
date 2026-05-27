package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.ContractTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ContractTypeRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetContractTypesComboboxQueryHandler
        implements QueryHandler<GetContractTypesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final ContractTypeRepository contractTypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetContractTypesComboboxQuery query) {
        var filter = new ContractTypeFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        if (query.getVinculoLaboralId() != null) {
            filter.setVinculoLaboralId(java.util.UUID.fromString(query.getVinculoLaboralId()));
        }
        var items = contractTypeRepository.findAll(filter).getData().stream()
                .map(ct -> new ComboboxItemDTO(ct.getId().getStringValor(), ct.getDescription()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
