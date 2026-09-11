package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.AssignmentRepository;
import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Lista os Lugares VAGOS (ocupáveis e sem afectação corrente) de uma unidade — serve o
 * picker de admissão do frontend. A ocupação é dona de colaboradores/ (Afectação), por
 * isso esta query vive aqui; os nomes (cargo/unidade/carreira/categoria) vêm do PositionMapper.
 */
@Component
@RequiredArgsConstructor
public class GetVagasListaUnidadeQueryHandler
        implements QueryHandler<GetVagasListaUnidadeQuery, ResponseEntity<WrapperListaPositionDTO>> {

    private final AssignmentRepository assignmentRepository;
    private final PositionRepository positionRepository;
    private final PositionMapper positionMapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaPositionDTO> handle(GetVagasListaUnidadeQuery query) {
        if (query.getUnidadeId() == null || query.getUnidadeId().isBlank())
            throw IgrpResponseStatusException.badRequest("A unidade é obrigatória.");

        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        List<Position> lugares = positionRepository.findByUnidade(unidadeId);

        long dotacao = lugares.stream().filter(Position::podeSerOcupado).count();

        List<PositionResponseDTO> vagos = lugares.stream()
                .filter(Position::podeSerOcupado)
                .filter(p -> !assignmentRepository.isPositionOccupied(p.getId().getValor()))
                .map(p -> {
                    PositionResponseDTO dto = positionMapper.toDTO(p);
                    dto.setOcupado(false);
                    return dto;
                })
                .toList();

        WrapperListaPositionDTO wrapper = new WrapperListaPositionDTO();
        wrapper.setContent(vagos);
        wrapper.setTotalElements(vagos.size());
        wrapper.setDotacao((int) dotacao);
        wrapper.setOcupados((int) (dotacao - vagos.size()));
        wrapper.setVagas(vagos.size());
        return ResponseEntity.ok(wrapper);
    }
}
