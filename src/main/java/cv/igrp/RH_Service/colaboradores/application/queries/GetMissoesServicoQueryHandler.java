package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.MissaoServicoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.MissaoServicoService;
import cv.igrp.RH_Service.colaboradores.domain.models.MissaoServico;
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
public class GetMissoesServicoQueryHandler implements QueryHandler<GetMissoesServicoQuery, ResponseEntity<List<MissaoServicoDTO>>> {

    private final MissaoServicoService service;
    private final MissaoServicoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<MissaoServicoDTO>> handle(GetMissoesServicoQuery q) {
        var lista = q.isComoMe() ? service.minhas(currentEmployeeResolver.resolve())
                : service.listar(ChecklistDtos.valor(MissaoServico.Estado.class, q.getEstado(), "Estado"),
                Entrada.uuidOpcional(q.getFuncionarioId(), "o colaborador") != null
                        ? FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")) : null);
        return ResponseEntity.ok(lista.stream().map(m -> dtos.dto(m, null)).toList());
    }
}
