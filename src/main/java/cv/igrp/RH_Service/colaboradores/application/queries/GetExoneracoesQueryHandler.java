package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ExoneracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.ExoneracaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.Exoneracao;
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
public class GetExoneracoesQueryHandler implements QueryHandler<GetExoneracoesQuery, ResponseEntity<List<ExoneracaoDTO>>> {

    private final ExoneracaoService service;
    private final ExoneracaoDtos dtos;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpQueryHandler
    public ResponseEntity<List<ExoneracaoDTO>> handle(GetExoneracoesQuery q) {
        List<Exoneracao> lista;
        if (q.isComoMe()) lista = service.doFuncionario(currentEmployeeResolver.resolve());
        else if (q.getFuncionarioId() != null) lista = service.doFuncionario(FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")));
        else lista = service.listar(ChecklistDtos.valor(Exoneracao.Estado.class, q.getEstado(), "Estado"));
        return ResponseEntity.ok(lista.stream().map(e -> dtos.dto(e, null)).toList());
    }
}
