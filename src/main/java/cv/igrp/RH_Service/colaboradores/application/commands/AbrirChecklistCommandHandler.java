package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ChecklistDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
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
public class AbrirChecklistCommandHandler implements CommandHandler<AbrirChecklistCommand, ResponseEntity<ChecklistDTO>> {

    private final ChecklistService service;

    @IgrpCommandHandler
    public ResponseEntity<ChecklistDTO> handle(AbrirChecklistCommand c) {
        var id = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        var r = Entrada.corpo(c.getRequest(), "o tipo (entrada ou saída)");
        var checklist = service.abrir(id, ChecklistDtos.valor(TipoChecklist.class, r.getTipo(), "Tipo de checklist"), r.getDataReferencia());
        return ResponseEntity.status(201).body(ChecklistDtos.dto(checklist, service.nomeDe(id), LocalDate.now()));
    }
}
