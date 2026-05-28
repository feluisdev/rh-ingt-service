package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoUploadResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.UploadDocumentoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ColaboradorDocumentoService;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsUploadDocumentoCommandHandler")
@RequiredArgsConstructor
public class UploadDocumentoCommandHandler
        implements CommandHandler<UploadDocumentoCommand, ResponseEntity<DocumentoUploadResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ColaboradorDocumentoService documentoService;

    @IgrpCommandHandler
    public ResponseEntity<DocumentoUploadResponseDTO> handle(UploadDocumentoCommand command) {
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        var dto = new UploadDocumentoRequestDTO();
        dto.setDocumentTypeId(command.getDocumentTypeId());
        dto.setFileKey(command.getFileKey());
        dto.setOriginalFilename(command.getOriginalFilename());
        dto.setContentType(command.getContentType());
        dto.setFileSize(command.getFileSize());
        dto.setDescription(command.getDescription());

        var saved = documentoService.registarDocumento(funcionarioId, dto);

        return ResponseEntity.status(201).body(new DocumentoUploadResponseDTO(
                saved.getId().getStringValor(),
                saved.getFileKey(),
                saved.getOriginalFilename(),
                saved.getContentType(),
                saved.getFileSize(),
                "Documento registado com sucesso"));
    }
}
