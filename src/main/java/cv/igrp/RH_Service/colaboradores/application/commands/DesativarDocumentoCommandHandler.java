package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsDesativarDocumentoCommandHandler")
@RequiredArgsConstructor
public class DesativarDocumentoCommandHandler
        implements CommandHandler<DesativarDocumentoCommand, ResponseEntity<Map<String, ?>>> {

    private final DocumentoRepository documentoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarDocumentoCommand command) {
        var docId = DocumentoId.from(command.getDocumentoId());
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var documento = documentoRepository.findById(docId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Documento não encontrado: " + command.getDocumentoId()));

        if (!documento.getReferenceId().equals(funcionarioId.getValor()))
            throw IgrpResponseStatusException.notFound(
                    "Documento não encontrado: " + command.getDocumentoId());

        // Idempotente: já inactivo → retorna 200 OK sem re-persistir
        if (Boolean.TRUE.equals(documento.getIsActive())) {
            documento.desativar();
            documentoRepository.save(documento);
        }

        return ResponseEntity.ok(Map.of("message", "Documento desactivado com sucesso"));
    }
}
