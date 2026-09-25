package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoEmitidoDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.DocumentosDtos;
import cv.igrp.RH_Service.colaboradores.application.services.EmissaoDocumentosService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnularDocumentoEmitidoCommandHandler implements CommandHandler<AnularDocumentoEmitidoCommand, ResponseEntity<DocumentoEmitidoDTO>> {

    private final EmissaoDocumentosService emissao;

    @IgrpCommandHandler
    public ResponseEntity<DocumentoEmitidoDTO> handle(AnularDocumentoEmitidoCommand command) {
        var id = DocumentoEmitidoId.from(Entrada.uuid(command.getDocumentoId(), "o documento"));
        return ResponseEntity.ok(DocumentosDtos.dto(emissao.anular(id, command.getRequest() != null ? command.getRequest().getMotivo() : null)));
    }
}
