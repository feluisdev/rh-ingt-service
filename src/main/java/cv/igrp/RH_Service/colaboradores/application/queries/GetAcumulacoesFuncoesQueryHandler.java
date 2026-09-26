package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AcumulacaoFuncoesDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AcumulacaoFuncoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.AcumulacaoFuncoes;
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
public class GetAcumulacoesFuncoesQueryHandler implements QueryHandler<GetAcumulacoesFuncoesQuery, ResponseEntity<List<AcumulacaoFuncoesDTO>>> {

    private final AcumulacaoFuncoesService service;
    private final AcumulacaoFuncoesDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<AcumulacaoFuncoesDTO>> handle(GetAcumulacoesFuncoesQuery q) {
        List<AcumulacaoFuncoes> lista;
        if (q.isComoMe()) lista = service.doFuncionario(currentEmployeeResolver.resolve());
        else if (q.getFuncionarioId() != null) lista = service.doFuncionario(FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")));
        else lista = service.listar(ChecklistDtos.valor(AcumulacaoFuncoes.Estado.class, q.getEstado(), "Estado"));
        return ResponseEntity.ok(lista.stream().map(dtos::dto).toList());
    }
}
