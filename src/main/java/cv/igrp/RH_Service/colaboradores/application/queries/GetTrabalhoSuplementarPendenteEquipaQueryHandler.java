package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.TrabalhoSuplementarDTO;
import cv.igrp.RH_Service.colaboradores.application.services.TrabalhoSuplementarService;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** A caixa da chefia: os pedidos de trabalho suplementar por decidir da sua equipa directa. */
@Component
@RequiredArgsConstructor
public class GetTrabalhoSuplementarPendenteEquipaQueryHandler
        implements QueryHandler<GetTrabalhoSuplementarPendenteEquipaQuery, ResponseEntity<List<TrabalhoSuplementarDTO>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final TrabalhoSuplementarService trabalhoSuplementarService;
    private final FuncionarioRepository funcionarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<TrabalhoSuplementarDTO>> handle(GetTrabalhoSuplementarPendenteEquipaQuery query) {
        var pendentes = trabalhoSuplementarService.pendentesDaEquipa(currentEmployeeResolver.resolve());
        Map<UUID, Funcionario> pessoas = funcionarioRepository.findAllByIds(
                        pendentes.stream().map(t -> t.getFuncionarioId().getValor()).distinct().toList()).stream()
                .collect(Collectors.toMap(f -> f.getId().getValor(), Function.identity(), (a, b) -> a));
        return ResponseEntity.ok(pendentes.stream()
                .map(t -> GetTrabalhoSuplementarMesQueryHandler.dto(t, pessoas.get(t.getFuncionarioId().getValor()),
                        null, 0, false))
                .toList());
    }
}
