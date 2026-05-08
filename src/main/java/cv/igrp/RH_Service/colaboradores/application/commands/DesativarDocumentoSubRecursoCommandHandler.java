package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarDocumentoSubRecursoCommandHandler")
@RequiredArgsConstructor
public class DesativarDocumentoSubRecursoCommandHandler
        implements CommandHandler<DesativarDocumentoSubRecursoCommand, ResponseEntity<Map<String, ?>>> {

    private final DocumentoRepository documentoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarDocumentoSubRecursoCommand command) {
        var docId = DocumentoId.from(command.getDocumentoId());

        var documento = documentoRepository.findById(docId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Documento não encontrado: " + command.getDocumentoId()));

        if (!command.getReferenceEntity().equals(documento.getReferenceEntity())
                || !command.getReferenceId().equals(documento.getReferenceId()))
            throw IgrpResponseStatusException.notFound("Documento não encontrado: " + command.getDocumentoId());

        if (Boolean.TRUE.equals(documento.getIsActive())) {
            documento.desativar();
            documentoRepository.save(documento);
        }

        return ResponseEntity.ok(Map.of("message", "Documento desactivado com sucesso"));
    }
}
