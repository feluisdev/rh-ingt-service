package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import cv.igrp.RH_Service.colaboradores.application.dto.VagasUnidadeResponseDTO;

/**
 * Vagas de uma unidade = dotação (Lugares ocupáveis) − ocupados (com afectação corrente).
 */
@Component
@RequiredArgsConstructor
public class GetVagasUnidadeQueryHandler
        implements QueryHandler<GetVagasUnidadeQuery, ResponseEntity<VagasUnidadeResponseDTO>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;

    @IgrpQueryHandler
    public ResponseEntity<VagasUnidadeResponseDTO> handle(GetVagasUnidadeQuery query) {
        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        List<Position> lugares = positionRepository.findByUnidade(unidadeId);

        long dotacao = lugares.stream().filter(Position::podeSerOcupado).count();
        long ocupados = lugares.stream()
                .filter(Position::podeSerOcupado)
                .filter(p -> assignmentRepository.temTitular(p.getId().getValor()))
                .count();

        return ResponseEntity.ok(new VagasUnidadeResponseDTO(
                query.getUnidadeId(), dotacao, ocupados, dotacao - ocupados));
    }
}
