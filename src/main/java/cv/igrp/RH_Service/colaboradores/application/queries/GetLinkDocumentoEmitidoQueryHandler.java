package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.services.EmissaoDocumentosService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.shared.application.dto.FileUrlDTO;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetLinkDocumentoEmitidoQueryHandler implements QueryHandler<GetLinkDocumentoEmitidoQuery, ResponseEntity<FileUrlDTO>> {

    private final EmissaoDocumentosService emissao;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<FileUrlDTO> handle(GetLinkDocumentoEmitidoQuery q) {
        var id = DocumentoEmitidoId.from(Entrada.uuid(q.getDocumentoId(), "o documento"));
        return ResponseEntity.ok(new FileUrlDTO(emissao.link(id, q.isDoProprio() ? currentEmployeeResolver.resolve() : null)));
    }
}
