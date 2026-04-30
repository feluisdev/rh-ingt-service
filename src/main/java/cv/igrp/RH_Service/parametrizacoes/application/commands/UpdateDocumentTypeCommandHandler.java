package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.DocumentTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.DocumentTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateDocumentTypeCommandHandler implements CommandHandler<UpdateDocumentTypeCommand, ResponseEntity<DocumentTypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateDocumentTypeCommandHandler.class);

    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentTypeMapper documentTypeMapper;

    @IgrpCommandHandler
    public ResponseEntity<DocumentTypeResponseDTO> handle(UpdateDocumentTypeCommand command) {
        var dto = command.getDocumentTypeRequest();
        var id = DocumentTypeId.from(java.util.UUID.fromString(command.getDocumentTypeId()));

        DocumentType documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de documento não encontrado: " + command.getDocumentTypeId()));

        documentType.atualizar(dto.getDescricao(), dto.getAllowedExtensions(), dto.getCategoryOptionId());

        DocumentType updated = documentTypeRepository.save(documentType);

        LOGGER.debug("DocumentType actualizado: {}", updated.getId().getStringValor());

        return ResponseEntity.ok(documentTypeMapper.toDTO(updated));
    }
}
