package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.PublicHolidayResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.PublicHolidayRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.PublicHolidayMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.PublicHolidayId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetPublicHolidayQueryHandler implements QueryHandler<GetPublicHolidayQuery, ResponseEntity<PublicHolidayResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetPublicHolidayQueryHandler.class);

    private final PublicHolidayRepository publicHolidayRepository;
    private final PublicHolidayMapper publicHolidayMapper;

    @IgrpQueryHandler
    public ResponseEntity<PublicHolidayResponseDTO> handle(GetPublicHolidayQuery query) {
        var id = PublicHolidayId.from(UUID.fromString(query.getPublicHolidayId()));

        return publicHolidayRepository.findById(id)
            .map(publicHolidayMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + query.getPublicHolidayId()));
    }
}
