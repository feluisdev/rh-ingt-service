package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.domain.filter.OrganizationalUnitFilter;
import cv.igrp.RH_Service.estrutura.domain.repository.OrganizationalUnitRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetOrganizationalUnitsComboboxQueryHandler
        implements QueryHandler<GetOrganizationalUnitsComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final OrganizationalUnitRepository organizationalUnitRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetOrganizationalUnitsComboboxQuery query) {
        var filter = new OrganizationalUnitFilter();
        filter.setIsActive(true);
        filter.setPage(0);
        filter.setSize(500);
        if (query.getParentUnitId() != null) {
            filter.setParentUnitId(java.util.UUID.fromString(query.getParentUnitId()));
        }
        var items = organizationalUnitRepository.findAll(filter).getData().stream()
                .map(u -> new ComboboxItemDTO(u.getId().getStringValor(), u.getName()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
