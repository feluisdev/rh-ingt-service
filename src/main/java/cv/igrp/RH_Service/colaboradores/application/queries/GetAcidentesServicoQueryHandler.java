package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AcidenteServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AcidenteServicoService;
import cv.igrp.RH_Service.colaboradores.domain.models.AcidenteServico;
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
public class GetAcidentesServicoQueryHandler implements QueryHandler<GetAcidentesServicoQuery, ResponseEntity<List<AcidenteServicoDTO>>> {

    private final AcidenteServicoService service;
    private final AcidenteServicoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<AcidenteServicoDTO>> handle(GetAcidentesServicoQuery q) {
        List<AcidenteServico> lista;
        if (q.isComoMe()) lista = service.doFuncionario(currentEmployeeResolver.resolve());
        else if (q.getFuncionarioId() != null) lista = service.doFuncionario(FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")));
        else lista = service.listar(ChecklistDtos.valor(AcidenteServico.Estado.class, q.getEstado(), "Estado"));
        return ResponseEntity.ok(lista.stream().map(a -> dtos.dto(a, null)).toList());
    }
}
