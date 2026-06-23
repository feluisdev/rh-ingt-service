package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.DocumentTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetDocumentTypeQueryHandler implements QueryHandler<GetDocumentTypeQuery, ResponseEntity<DocumentTypeResponseDTO>> {

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<DocumentTypeResponseDTO> handle(GetDocumentTypeQuery query) {
        var id = DocumentTypeId.from(java.util.UUID.fromString(query.getDocumentTypeId()));

        var documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de documento não encontrado: " + query.getDocumentTypeId()));

        var dto = documentTypeMapper.toDTO(documentType);
        if (documentType.getCategory() != null) {
            optionLookupPort.findByCcodeAndCkey(OptionCcode.DOC_CATEGORY.getCode(), documentType.getCategory())
                    .ifPresent(opt -> dto.setCategoryDesc(opt.cvalue()));
        }
        return ResponseEntity.ok(dto);
    }
}
