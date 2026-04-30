package cv.igrp.RH_Service.parametrizacoes.application.queries;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ProfessionalSituationResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ProfessionalSituationMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ProfessionalSituationId;
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
public class GetProfessionalSituationQueryHandler implements QueryHandler<GetProfessionalSituationQuery, ResponseEntity<ProfessionalSituationResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetProfessionalSituationQueryHandler.class);

    private final ProfessionalSituationRepository professionalSituationRepository;
    private final ProfessionalSituationMapper professionalSituationMapper;

    @IgrpQueryHandler
    public ResponseEntity<ProfessionalSituationResponseDTO> handle(GetProfessionalSituationQuery query) {
        var id = ProfessionalSituationId.from(UUID.fromString(query.getProfessionalSituationId()));

        return professionalSituationRepository.findById(id)
            .map(professionalSituationMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + query.getProfessionalSituationId()));
    }
}
