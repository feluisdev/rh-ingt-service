package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ProfessionalSituationId;
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
public class DesativarProfessionalSituationCommandHandler implements CommandHandler<DesativarProfessionalSituationCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesativarProfessionalSituationCommandHandler.class);

    private final ProfessionalSituationRepository professionalSituationRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarProfessionalSituationCommand command) {
        var id = ProfessionalSituationId.from(UUID.fromString(command.getProfessionalSituationId()));

        ProfessionalSituation professionalSituation = professionalSituationRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Não encontrado: " + command.getProfessionalSituationId()));

        professionalSituation.desativar();
        professionalSituationRepository.save(professionalSituation);

        return ResponseEntity.ok(Map.of("message", "Desativado com sucesso"));
    }
}
