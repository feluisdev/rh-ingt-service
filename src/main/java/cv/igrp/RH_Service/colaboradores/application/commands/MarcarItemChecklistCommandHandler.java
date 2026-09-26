package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.Checklist;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ChecklistId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistId;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class MarcarItemChecklistCommandHandler implements CommandHandler<MarcarItemChecklistCommand, ResponseEntity<ChecklistDTO>> {

    private final ChecklistService service;
    private final CurrentEmployeeResolver currentEmployeeResolver;

    @IgrpCommandHandler
    public ResponseEntity<ChecklistDTO> handle(MarcarItemChecklistCommand c) {
        var cid = ChecklistId.from(Entrada.uuid(c.getChecklistId(), "a checklist"));
        var itemId = ItemChecklistId.from(Entrada.uuid(c.getItemId(), "o item"));
        var r = Entrada.corpo(c.getRequest(), "o estado do item (feito, não aplicável ou pendente)");
        var estado = ChecklistDtos.valor(Checklist.EstadoItem.class, r.getEstado(), "Estado do item");
        Checklist checklist = c.getFuncionarioId() == null
                ? service.marcarComo(currentEmployeeResolver.resolve(), cid, itemId, estado, r.getObservacao(), r.getData())
                : service.marcar(FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador")), cid, itemId, estado,
                r.getObservacao(), r.getData());
        return ResponseEntity.ok(ChecklistDtos.dto(checklist, service.nomeDe(checklist.getFuncionarioId()), LocalDate.now()));
    }
}
