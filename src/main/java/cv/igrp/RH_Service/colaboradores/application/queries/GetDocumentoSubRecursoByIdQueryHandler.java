package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetDocumentoSubRecursoByIdQueryHandler")
@RequiredArgsConstructor
public class GetDocumentoSubRecursoByIdQueryHandler
        implements QueryHandler<GetDocumentoSubRecursoByIdQuery, ResponseEntity<DocumentoResponseDTO>> {

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<DocumentoResponseDTO> handle(GetDocumentoSubRecursoByIdQuery query) {
        var docId = DocumentoId.from(query.getDocumentoId());

        var documento = documentoRepository.findById(docId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Documento não encontrado: " + query.getDocumentoId()));

        if (!query.getReferenceEntity().equals(documento.getReferenceEntity())
                || !query.getReferenceId().equals(documento.getReferenceId()))
            throw IgrpResponseStatusException.notFound("Documento não encontrado: " + query.getDocumentoId());

        if (Boolean.FALSE.equals(documento.getIsActive()))
            throw IgrpResponseStatusException.notFound("Documento não encontrado: " + query.getDocumentoId());

        return ResponseEntity.ok(mapper.toDTO(documento));
    }
}
