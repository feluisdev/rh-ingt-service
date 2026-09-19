package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class DesativarDocumentTypeCommandHandler implements CommandHandler<DesativarDocumentTypeCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarDocumentTypeCommandHandler.class);

    private final DocumentTypeRepository documentTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarDocumentTypeCommand command) {
        var id = DocumentTypeId.from(java.util.UUID.fromString(command.getDocumentTypeId()));

        DocumentType documentType = documentTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de documento não encontrado: " + command.getDocumentTypeId()));

        documentType.desativar();
        documentTypeRepository.save(documentType);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getDocumentTypeId()));
    }
}
