package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.DocumentTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetDocumentTypeQueryHandler implements QueryHandler<GetDocumentTypeQuery, ResponseEntity<DocumentTypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDocumentTypeQueryHandler.class);

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;

    @IgrpQueryHandler
    public ResponseEntity<DocumentTypeResponseDTO> handle(GetDocumentTypeQuery query) {
        var id = DocumentTypeId.from(java.util.UUID.fromString(query.getDocumentTypeId()));

        var documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de documento não encontrado: " + query.getDocumentTypeId()));

        return ResponseEntity.ok(documentTypeMapper.toDTO(documentType));
    }
}
