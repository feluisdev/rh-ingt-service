package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ProfessionalSituation;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.ProfessionalSituationRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateProfessionalSituationCommandHandler implements CommandHandler<CreateProfessionalSituationCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateProfessionalSituationCommandHandler.class);

    private final ProfessionalSituationRepository professionalSituationRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateProfessionalSituationCommand command) {
        var dto = command.getProfessionalSituationRequest();

        if (professionalSituationRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um registo com code='" + dto.getCode() + "'.");
        }

        ProfessionalSituation saved = professionalSituationRepository.save(
            ProfessionalSituation.criar(dto.getCode(), dto.getDescription(),
                Boolean.TRUE.equals(dto.getCountsSeniority()),
                Boolean.TRUE.equals(dto.getEligibleForProgression()))
        );

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId().getStringValor(),
            "message", "Criado com sucesso"
        ));
    }
}
