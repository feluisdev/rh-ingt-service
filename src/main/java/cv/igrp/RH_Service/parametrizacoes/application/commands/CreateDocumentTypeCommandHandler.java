package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.DocumentType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateDocumentTypeCommandHandler implements CommandHandler<CreateDocumentTypeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateDocumentTypeCommandHandler.class);

    private final DocumentTypeRepository documentTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateDocumentTypeCommand command) {
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

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId().getStringValor(),
            "message", "Tipo de documento criado com sucesso"
        ));
    }
}
