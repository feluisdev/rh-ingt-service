package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GetChecklistsQueryHandler implements QueryHandler<GetChecklistsQuery, ResponseEntity<List<ChecklistDTO>>> {

    private final ChecklistService service;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<ChecklistDTO>> handle(GetChecklistsQuery q) {
        var hoje = LocalDate.now();
        var lista = q.getFuncionarioId() == null ? service.minhas(currentEmployeeResolver.resolve())
                : service.doFuncionario(FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")));
        return ResponseEntity.ok(lista.stream().map(c -> ChecklistDtos.dto(c, service.nomeDe(c.getFuncionarioId()), hoje)).toList());
    }
}
