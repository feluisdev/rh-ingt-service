package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoEmitidoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoEmitidoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetDocumentosEmitidosQueryHandler implements QueryHandler<GetDocumentosEmitidosQuery, ResponseEntity<List<DocumentoEmitidoDTO>>> {

    private final DocumentoEmitidoRepository repository;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<DocumentoEmitidoDTO>> handle(GetDocumentosEmitidosQuery q) {
        FuncionarioId f = q.getFuncionarioId() == null ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador"));
        return ResponseEntity.ok(repository.findByFuncionario(f).stream().map(DocumentosDtos::dto).toList());
    }
}
