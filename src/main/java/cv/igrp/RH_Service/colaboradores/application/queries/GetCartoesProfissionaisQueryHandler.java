package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.CartaoProfissionalDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CartaoProfissionalService;
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
public class GetCartoesProfissionaisQueryHandler implements QueryHandler<GetCartoesProfissionaisQuery, ResponseEntity<List<CartaoProfissionalDTO>>> {

    private final CartaoProfissionalService service;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<CartaoProfissionalDTO>> handle(GetCartoesProfissionaisQuery q) {
        FuncionarioId f = q.getFuncionarioId() == null ? currentEmployeeResolver.resolve()
                : FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador"));
        return ResponseEntity.ok(service.doFuncionario(f).stream().map(CartoesDtos::dto).toList());
    }
}
