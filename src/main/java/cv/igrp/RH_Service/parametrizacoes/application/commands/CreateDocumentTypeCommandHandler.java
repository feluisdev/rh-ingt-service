package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
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
public class CreateDocumentTypeCommandHandler implements CommandHandler<CreateDocumentTypeCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDocumentTypeCommandHandler.class);

    private final DocumentTypeRepository documentTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateDocumentTypeCommand command) {
        var dto = command.getDocumentTypeRequest();

        if (documentTypeRepository.existsByCodigo(dto.getCodigo())) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um tipo de documento com o código: '" + dto.getCodigo() + "'.");
        }

        DocumentType documentType = DocumentType.criar(
            dto.getCodigo(),
            dto.getDescricao(),
            dto.getAllowedExtensions(),
            dto.getCategory()
        );

        DocumentType saved = documentTypeRepository.save(documentType);

        LOGGER.debug("DocumentType criado com id: {}", saved.getId().getStringValor());

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
