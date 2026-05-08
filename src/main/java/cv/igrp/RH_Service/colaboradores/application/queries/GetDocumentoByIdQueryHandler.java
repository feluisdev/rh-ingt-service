package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetDocumentoByIdQueryHandler")
@RequiredArgsConstructor
public class GetDocumentoByIdQueryHandler
        implements QueryHandler<GetDocumentoByIdQuery, ResponseEntity<DocumentoResponseDTO>> {

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<DocumentoResponseDTO> handle(GetDocumentoByIdQuery query) {
        var docId = DocumentoId.from(query.getDocumentoId());
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());

        var documento = documentoRepository.findById(docId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Documento não encontrado: " + query.getDocumentoId()));

        if (!documento.getReferenceId().equals(funcionarioId.getValor()))
            throw IgrpResponseStatusException.notFound(
                    "Documento não encontrado: " + query.getDocumentoId());

        if (Boolean.FALSE.equals(documento.getIsActive()))
            throw IgrpResponseStatusException.notFound(
                    "Documento não encontrado: " + query.getDocumentoId());

        return ResponseEntity.ok(mapper.toDTO(documento));
    }
}
