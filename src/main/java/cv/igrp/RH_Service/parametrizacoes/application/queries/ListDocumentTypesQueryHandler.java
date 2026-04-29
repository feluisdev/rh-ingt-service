package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.WrapperListaDocumentTypeDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.filter.DocumentTypeFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.DocumentTypeMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListDocumentTypesQueryHandler implements QueryHandler<ListDocumentTypesQuery, ResponseEntity<WrapperListaDocumentTypeDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListDocumentTypesQueryHandler.class);

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDocumentTypeDTO> handle(ListDocumentTypesQuery query) {
        var filter = new DocumentTypeFilter();
        filter.setCodigo(query.getCodigo());
        filter.setActive(query.getActive());
        filter.setPage(query.getPagina() != null ? Integer.parseInt(query.getPagina()) : 0);
        filter.setSize(query.getTamanho() != null ? Integer.parseInt(query.getTamanho()) : 20);

        List<DocumentType> documentTypes = documentTypeRepository.findAll(filter);

        var content = documentTypes.stream().map(documentTypeMapper::toDTO).toList();

        var wrapper = new WrapperListaDocumentTypeDTO();
        wrapper.setContent(new java.util.ArrayList<>(content));
        wrapper.setTotalElements((long) content.size());

        return ResponseEntity.ok(wrapper);
    }
}
