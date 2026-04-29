package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.OptionResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.OptionRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.OptionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetOptionQueryHandler implements QueryHandler<GetOptionQuery, ResponseEntity<OptionResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetOptionQueryHandler.class);

    private final OptionRepository optionRepository;
    private final OptionMapper optionMapper;

    @IgrpQueryHandler
    public ResponseEntity<OptionResponseDTO> handle(GetOptionQuery query) {
        var id = ExternalID.from(java.util.UUID.fromString(query.getOptionId()));

        var option = optionRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Etiqueta não encontrada: " + query.getOptionId()));

        return ResponseEntity.ok(optionMapper.toDTO(option));
    }
}
