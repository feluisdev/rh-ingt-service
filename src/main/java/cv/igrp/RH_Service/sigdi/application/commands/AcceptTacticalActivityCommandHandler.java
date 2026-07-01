package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.models.TacticalActivity;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.TacticalActivityRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.valueobject.TacticalActivityId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AcceptTacticalActivityCommandHandler implements CommandHandler<AcceptTacticalActivityCommand, ResponseEntity<TacticalActivityResponseDTO>> {

    private final TacticalActivityRepository repository;

    public AcceptTacticalActivityCommandHandler(TacticalActivityRepository repository) {
        this.repository = repository;
    }

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<TacticalActivityResponseDTO> handle(AcceptTacticalActivityCommand command) {
        TacticalActivityId id = TacticalActivityId.from(command.getId());
        TacticalActivity activity = repository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Atividade tática não encontrada"));

        TacticalActivity accepted = activity.accept();
        TacticalActivity saved = repository.save(accepted);

        TacticalActivityResponseDTO response = new TacticalActivityResponseDTO();
        response.setId(saved.getId().getValor().getValor());
        response.setStrategicGoalId(saved.getStrategicGoalId().getValor().getValor());
        response.setOrganicUnitId(saved.getOrganicUnitId());
        response.setTitle(saved.getTitle());
        response.setDescriptionWhat(saved.getDescriptionWhat());
        response.setJustificationWhy(saved.getJustificationWhy());
        response.setLocationWhere(saved.getLocationWhere());
        response.setResponsibleWho(saved.getResponsibleWho());
        response.setMethodologyHow(saved.getMethodologyHow());
        response.setStartDate(saved.getDateRange().getStartDate());
        response.setEndDate(saved.getDateRange().getEndDate());
        response.setStatus(saved.getStatus().getCode());
        response.setStatusDesc(saved.getStatus().getDescription());
        response.setPaaLevel(saved.getPaaLevel().getCode());
        response.setPaaLevelDesc(saved.getPaaLevel().getDescription());

        if (saved.getAcceptanceStatus() != null) {
            response.setAcceptanceStatus(saved.getAcceptanceStatus().getCode());
            response.setAcceptanceStatusDesc(saved.getAcceptanceStatus().getDescription());
        }

        return ResponseEntity.ok(response);
    }
}
