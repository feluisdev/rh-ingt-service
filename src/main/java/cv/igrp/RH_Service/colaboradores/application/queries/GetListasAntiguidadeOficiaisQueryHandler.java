package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CicloListaAntiguidadeService;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetListasAntiguidadeOficiaisQueryHandler
        implements QueryHandler<GetListasAntiguidadeOficiaisQuery, ResponseEntity<List<ListaAntiguidadeOficialDTO>>> {

    private final CicloListaAntiguidadeService service;

    @IgrpQueryHandler
    public ResponseEntity<List<ListaAntiguidadeOficialDTO>> handle(GetListasAntiguidadeOficiaisQuery q) {
        var unidade = Entrada.uuidOpcional(q.getUnidadeId(), "a unidade orgânica");
        return ResponseEntity.ok(service.listar(q.getAno(), unidade).stream()
                .map(l -> ListasAntiguidadeDtos.dto(l, null, false, null, null, List.of())).toList());
    }
}
