package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Lista os Lugares de uma unidade. A ocupação (ocupados/vagas) é resolvida pela
 * query equivalente em colaboradores/, que é dona da afectação; aqui devolve-se
 * a dotação (nº de Lugares) da unidade.
 */
@Component
@RequiredArgsConstructor
public class GetPositionsByUnidadeQueryHandler
        implements QueryHandler<GetPositionsByUnidadeQuery, ResponseEntity<WrapperListaPositionDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPositionsByUnidadeQueryHandler.class);

    private final PositionRepository positionRepository;
    private final PositionMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaPositionDTO> handle(GetPositionsByUnidadeQuery query) {
        if (query.getUnidadeId() == null || query.getUnidadeId().isBlank())
            throw IgrpResponseStatusException.badRequest("A unidade é obrigatória.");

        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        List<PositionResponseDTO> content = positionRepository.findByUnidade(unidadeId)
                .stream().map(mapper::toDTO).toList();

        WrapperListaPositionDTO wrapper = new WrapperListaPositionDTO();
        wrapper.setContent(content);
        wrapper.setTotalElements(content.size());
        wrapper.setDotacao(content.size());
        return ResponseEntity.ok(wrapper);
    }
}
