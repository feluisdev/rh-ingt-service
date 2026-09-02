package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Vagas de uma unidade = dotação (Lugares ocupáveis) − ocupados (com afectação corrente).
 */
@Component
@RequiredArgsConstructor
public class GetVagasUnidadeQueryHandler
        implements QueryHandler<GetVagasUnidadeQuery, ResponseEntity<Map<String, Object>>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;

    @IgrpQueryHandler
    public ResponseEntity<Map<String, Object>> handle(GetVagasUnidadeQuery query) {
        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        List<Position> lugares = positionRepository.findByUnidade(unidadeId);

        long dotacao = lugares.stream().filter(Position::podeSerOcupado).count();
        long ocupados = lugares.stream()
                .filter(Position::podeSerOcupado)
                .filter(p -> assignmentRepository.isPositionOccupied(p.getId().getValor()))
                .count();

        Map<String, Object> body = new HashMap<>();
        body.put("unidadeId", query.getUnidadeId());
        body.put("dotacao", dotacao);
        body.put("ocupados", ocupados);
        body.put("vagas", dotacao - ocupados);
        return ResponseEntity.ok(body);
    }
}
