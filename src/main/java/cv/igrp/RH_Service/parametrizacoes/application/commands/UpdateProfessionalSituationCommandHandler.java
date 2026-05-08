package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.ProfessionalSituationResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.ProfessionalSituationMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ProfessionalSituationId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdateProfessionalSituationCommandHandler implements CommandHandler<UpdateProfessionalSituationCommand, ResponseEntity<ProfessionalSituationResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateProfessionalSituationCommandHandler.class);

    private final ProfessionalSituationRepository professionalSituationRepository;
    private final ProfessionalSituationMapper professionalSituationMapper;

    @IgrpCommandHandler
    public ResponseEntity<ProfessionalSituationResponseDTO> handle(UpdateProfessionalSituationCommand command) {
        var id = ProfessionalSituationId.from(UUID.fromString(command.getProfessionalSituationId()));

        ProfessionalSituation professionalSituation = professionalSituationRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getProfessionalSituationId()));

        var req = command.getProfessionalSituationRequest();
        professionalSituation.atualizar(
            req.getDescription(),
            Boolean.TRUE.equals(req.getCountsSeniority()),
            Boolean.TRUE.equals(req.getEligibleForProgression())
        );
        ProfessionalSituation saved = professionalSituationRepository.save(professionalSituation);

        return ResponseEntity.ok(professionalSituationMapper.toDTO(saved));
    }
}
