package cv.igrp.RH_Service.sigdi.application.commands;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.dto.TacticalActivityResponseDTO;
import cv.igrp.RH_Service.sigdi.application.service.ActivityApprovalHistoryRecorder;
import cv.igrp.RH_Service.sigdi.application.service.PaaActivityWindowPolicy;
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
    private final PaaActivityWindowPolicy windowPolicy;
    private final ActivityApprovalHistoryRecorder historyRecorder;

    public AcceptTacticalActivityCommandHandler(TacticalActivityRepository repository,
            PaaActivityWindowPolicy windowPolicy,
            ActivityApprovalHistoryRecorder historyRecorder) {
        this.repository = repository;
        this.windowPolicy = windowPolicy;
        this.historyRecorder = historyRecorder;
    }

    @IgrpCommandHandler
    @Transactional
    @Override
    public ResponseEntity<TacticalActivityResponseDTO> handle(AcceptTacticalActivityCommand command) {
        TacticalActivityId id = TacticalActivityId.from(command.getId());
        TacticalActivity activity = repository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Atividade tática não encontrada"));

        // A-132-111 / POR-02 (Phase 134): submission window checked before the state transition,
        // sourced from the loaded entity's paaLevel -- the client cannot pick it (D-27).
        windowPolicy.requireOpenFor(activity.getPaaLevel());

        // A-135-2AB (Phase 136, plano 136-10): accept() transiciona acceptanceStatus, não
        // status -- é esse o campo capturado antes da transição para servir de fromStatus.
        String previousAcceptanceStatus = activity.getAcceptanceStatus() != null
                ? activity.getAcceptanceStatus().getCode()
                : null;

        TacticalActivity accepted = activity.accept();
        TacticalActivity saved = repository.save(accepted);

        // Rasto de auditoria escrito depois do save, dentro da mesma fronteira @Transactional --
        // nunca antes, porque gravaria histórico de uma transição que ainda podia falhar. Usa a
        // instância devolvida pela própria transição (accepted), não o retorno do repositório
        // (saved), para não depender do que o mock/adapter de save() decidir devolver.
        historyRecorder.record(accepted.getId(), previousAcceptanceStatus,
                accepted.getAcceptanceStatus().getCode(), accepted.getAcceptanceStatus().getCode(), null);

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
