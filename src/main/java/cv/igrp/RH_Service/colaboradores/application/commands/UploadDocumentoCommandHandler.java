package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoUploadResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Documento;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.DocumentTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.DocumentTypeId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component("colabsUploadDocumentoCommandHandler")
@RequiredArgsConstructor
public class UploadDocumentoCommandHandler
        implements CommandHandler<UploadDocumentoCommand, ResponseEntity<DocumentoUploadResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final DocumentTypeRepository documentTypeRepository;
    private final DocumentoRepository documentoRepository;

    @IgrpCommandHandler
    public ResponseEntity<DocumentoUploadResponseDTO> handle(UploadDocumentoCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        var tipoId = DocumentTypeId.from(command.getDocumentTypeId());
        var tipo = documentTypeRepository.findById(tipoId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Tipo de documento não encontrado: " + command.getDocumentTypeId()));

        if (!tipo.isActive())
            throw IgrpResponseStatusException.badRequest(
                    "Tipo de documento inactivo: " + tipo.getCodigo());

        var extension = FilenameUtils.getExtension(command.getOriginalFilename()).toLowerCase();
        var allowed = Arrays.stream(tipo.getAllowedExtensions().split(","))
                .map(String::trim).map(String::toLowerCase).toList();
        if (!allowed.contains(extension))
            throw IgrpResponseStatusException.badRequest(
                    "Extensão não permitida. Aceites: " + tipo.getAllowedExtensions());

        var refId = command.getReferenceId() != null
                ? command.getReferenceId()
                : funcionarioId.getValor();

        var saved = documentoRepository.save(Documento.criar(
                command.getReferenceEntity(), refId, tipoId,
                command.getFileKey(),
                command.getOriginalFilename(),
                command.getContentType(),
                command.getFileSize(),
                command.getDescription()));

        return ResponseEntity.status(201).body(new DocumentoUploadResponseDTO(
                saved.getId().getStringValor(),
                saved.getFileKey(),
                saved.getOriginalFilename(),
                saved.getContentType(),
                saved.getFileSize(),
                "Documento registado com sucesso"));
    }
}
