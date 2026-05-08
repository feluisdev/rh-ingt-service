package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoDownloadResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.DocumentoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetDocumentoDownloadSubRecursoQueryHandler")
@RequiredArgsConstructor
public class GetDocumentoDownloadSubRecursoQueryHandler
        implements QueryHandler<GetDocumentoDownloadSubRecursoQuery, ResponseEntity<DocumentoDownloadResponseDTO>> {

    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;

    @Value("${igrp.minio.url-expiration-time:3600}")
    private long urlExpirationTime;

    @IgrpQueryHandler
    public ResponseEntity<DocumentoDownloadResponseDTO> handle(GetDocumentoDownloadSubRecursoQuery query) {
        var docId = DocumentoId.from(query.getDocumentoId());

        var documento = documentoRepository.findById(docId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Documento não encontrado: " + query.getDocumentoId()));

        if (!query.getReferenceEntity().equals(documento.getReferenceEntity())
                || !query.getReferenceId().equals(documento.getReferenceId()))
            throw IgrpResponseStatusException.notFound("Documento não encontrado: " + query.getDocumentoId());

        if (Boolean.FALSE.equals(documento.getIsActive()))
            throw IgrpResponseStatusException.notFound("Documento não encontrado: " + query.getDocumentoId());

        var urlResult = documentoService.getPresignedLink(documento.getFileKey());
        return ResponseEntity.ok(new DocumentoDownloadResponseDTO(urlResult.getBody().getUrl(), urlExpirationTime));
    }
}
