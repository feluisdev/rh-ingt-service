package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ItemChecklistModeloDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.colaboradores.domain.models.ResponsavelChecklist;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoChecklist;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ItemChecklistModeloId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GuardarItemChecklistModeloCommandHandler
        implements CommandHandler<GuardarItemChecklistModeloCommand, ResponseEntity<ItemChecklistModeloDTO>> {

    private final ChecklistService service;

    @IgrpCommandHandler
    public ResponseEntity<ItemChecklistModeloDTO> handle(GuardarItemChecklistModeloCommand c) {
        var r = Entrada.corpo(c.getRequest(), "a descrição e o responsável");
        var responsavel = ChecklistDtos.valor(ResponsavelChecklist.class, r.getResponsavel(), "Responsável");
        if (c.getItemId() == null)
            return ResponseEntity.status(201).body(ChecklistDtos.dto(service.criarModelo(
                    ChecklistDtos.valor(TipoChecklist.class, r.getTipo(), "Tipo de checklist"), r.getCodigo(), r.getDescricao(),
                    responsavel, Boolean.TRUE.equals(r.getObrigatorio()), r.getPrazoDias(), r.getOrdem())));
        var id = ItemChecklistModeloId.from(Entrada.uuid(c.getItemId(), "o item do modelo"));
        return ResponseEntity.ok(ChecklistDtos.dto(service.actualizarModelo(id, r.getDescricao(), responsavel, r.getObrigatorio(),
                r.getPrazoDias(), r.getOrdem(), r.getActivo())));
    }
}
