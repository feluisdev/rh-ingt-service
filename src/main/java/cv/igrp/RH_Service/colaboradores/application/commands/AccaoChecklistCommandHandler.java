package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class AccaoChecklistCommandHandler implements CommandHandler<AccaoChecklistCommand, ResponseEntity<ChecklistDTO>> {

    private final ChecklistService service;

    @IgrpCommandHandler
    public ResponseEntity<ChecklistDTO> handle(AccaoChecklistCommand c) {
        var id = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        var cid = ChecklistId.from(Entrada.uuid(c.getChecklistId(), "a checklist"));
        ChecklistRequestDTO r = c.getRequest() != null ? c.getRequest() : new ChecklistRequestDTO();
        var checklist = switch (c.getAccao()) {
            case "ACRESCENTAR" -> service.acrescentar(id, cid, r.getDescricao(),
                    ChecklistDtos.valor(ResponsavelChecklist.class, r.getResponsavel(), "Responsável"),
                    Boolean.TRUE.equals(r.getObrigatorio()), r.getPrazo());
            case "CANCELAR" -> service.cancelar(id, cid, r.getMotivo());
            default -> throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
        };
        return ResponseEntity.ok(ChecklistDtos.dto(checklist, service.nomeDe(id), LocalDate.now()));
    }
}
