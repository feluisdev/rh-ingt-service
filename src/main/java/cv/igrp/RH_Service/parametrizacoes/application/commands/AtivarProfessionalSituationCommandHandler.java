package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AtivarProfessionalSituationCommandHandler implements CommandHandler<AtivarProfessionalSituationCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarProfessionalSituationCommandHandler.class);

    private final ProfessionalSituationRepository professionalSituationRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarProfessionalSituationCommand command) {
        var id = ExternalID.from(UUID.fromString(command.getProfessionalSituationId()));

        ProfessionalSituation professionalSituation = professionalSituationRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getProfessionalSituationId()));

        professionalSituation.reativar();
        professionalSituationRepository.save(professionalSituation);

        return ResponseEntity.ok(Map.of("message", "Ativado com sucesso"));
    }
}
