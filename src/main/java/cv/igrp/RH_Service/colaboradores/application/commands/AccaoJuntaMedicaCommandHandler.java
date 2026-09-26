package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.JuntaMedicaRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.queries.SaudeTrabalhoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.colaboradores.domain.models.JuntaMedica;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.JuntaMedicaId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccaoJuntaMedicaCommandHandler implements CommandHandler<AccaoJuntaMedicaCommand, ResponseEntity<JuntaMedicaDTO>> {

    private final SaudeTrabalhoService service;
    private final SaudeTrabalhoDtos dtos;

    @IgrpCommandHandler
    public ResponseEntity<JuntaMedicaDTO> handle(AccaoJuntaMedicaCommand c) {
        var fid = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        JuntaMedicaRequestDTO r = c.getRequest() != null ? c.getRequest() : new JuntaMedicaRequestDTO();
        if ("PEDIR".equals(c.getAccao())) {
            var j = service.pedirJunta(fid, ChecklistDtos.valor(JuntaMedica.Motivo.class, r.getMotivo(), "Motivo"), r.getFundamentacao(), r.getData());
            return ResponseEntity.status(201).body(dtos.dto(j, null));
        }
        var id = JuntaMedicaId.from(Entrada.uuid(c.getJuntaId(), "o pedido de junta"));
        if ("PARECER".equals(c.getAccao())) {
            var res = service.registarParecer(fid, id, r.getData(), ChecklistDtos.valor(JuntaMedica.Parecer.class, r.getParecer(), "Parecer"),
                    r.getDiasIncapacidade(), r.getObservacoes());
            return ResponseEntity.ok(dtos.dto(res.valor(), res.alertas()));
        }
        if ("CANCELAR".equals(c.getAccao())) return ResponseEntity.ok(dtos.dto(service.cancelarJunta(fid, id), null));
        throw new IllegalArgumentException("Acção desconhecida: " + c.getAccao());
    }
}
