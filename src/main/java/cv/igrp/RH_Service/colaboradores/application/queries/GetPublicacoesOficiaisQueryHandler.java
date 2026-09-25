package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.PublicacaoOficialDTO;
import cv.igrp.RH_Service.colaboradores.application.services.PublicacoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetPublicacoesOficiaisQueryHandler implements QueryHandler<GetPublicacoesOficiaisQuery, ResponseEntity<List<PublicacaoOficialDTO>>> {

    private final PublicacoesService service;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<PublicacaoOficialDTO>> handle(GetPublicacoesOficiaisQuery q) {
        var lista = q.getFuncionarioId() != null
                ? service.doFuncionario(FuncionarioId.from(Entrada.uuid(q.getFuncionarioId(), "o colaborador")))
                : service.listar(PublicacoesDtos.enumOuNulo(PublicacaoOficial.Estado.class, q.getEstado(), "Estado"));
        return ResponseEntity.ok(lista.stream().map(p -> PublicacoesDtos.dto(p, funcionarioRepository)).toList());
    }
}
