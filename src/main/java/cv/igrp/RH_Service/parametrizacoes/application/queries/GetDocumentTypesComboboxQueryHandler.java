package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.DocumentTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.shared.application.dto.ComboboxItemDTO;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetDocumentTypesComboboxQueryHandler
        implements QueryHandler<GetDocumentTypesComboboxQuery, ResponseEntity<List<ComboboxItemDTO>>> {

    private final DocumentTypeRepository documentTypeRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<ComboboxItemDTO>> handle(GetDocumentTypesComboboxQuery query) {
        var filter = new DocumentTypeFilter();
        filter.setActive(true);
        filter.setPage(0);
        filter.setSize(500);
        var items = documentTypeRepository.findAll(filter).getData().stream()
                .map(dt -> new ComboboxItemDTO(dt.getId().getStringValor(), dt.getDescricao()))
                .toList();
        return ResponseEntity.ok(items);
    }
}
