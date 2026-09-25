package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ListaAntiguidadeOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.services.CicloListaAntiguidadeService;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** As listas onde o utilizador aparece — só a sua linha e as suas reclamações. */
@Component
@RequiredArgsConstructor
public class GetMinhasListasAntiguidadeQueryHandler
        implements QueryHandler<GetMinhasListasAntiguidadeQuery, ResponseEntity<List<ListaAntiguidadeOficialDTO>>> {

    private final CicloListaAntiguidadeService service;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<ListaAntiguidadeOficialDTO>> handle(GetMinhasListasAntiguidadeQuery q) {
        var eu = currentEmployeeResolver.resolve();
        return ResponseEntity.ok(service.visiveisPara(eu).stream()
                .map(l -> ListasAntiguidadeDtos.dto(l, service.reclamacoes(l.getId()), true, eu, null, List.of())).toList());
    }
}
