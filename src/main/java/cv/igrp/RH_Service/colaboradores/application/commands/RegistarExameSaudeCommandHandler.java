package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.ExameSaudeDTO;
import cv.igrp.RH_Service.colaboradores.application.queries.ChecklistDtos;
import cv.igrp.RH_Service.colaboradores.application.queries.SaudeTrabalhoDtos;
import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.colaboradores.domain.models.ExameSaude;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.service.Entrada;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegistarExameSaudeCommandHandler implements CommandHandler<RegistarExameSaudeCommand, ResponseEntity<ExameSaudeDTO>> {

    private final SaudeTrabalhoService service;
    private final SaudeTrabalhoDtos dtos;

    @IgrpCommandHandler
    public ResponseEntity<ExameSaudeDTO> handle(RegistarExameSaudeCommand c) {
        var fid = FuncionarioId.from(Entrada.uuid(c.getFuncionarioId(), "o colaborador"));
        var r = Entrada.corpo(c.getRequest(), "o tipo, a data e o resultado do exame");
        var res = service.registarExame(fid, ChecklistDtos.valor(ExameSaude.Tipo.class, r.getTipo(), "Tipo de exame"), r.getData(), r.getEntidade(),
                ChecklistDtos.valor(ExameSaude.Resultado.class, r.getResultado(), "Resultado"), r.getRestricoes(), r.getValidadeAte(), r.getObservacoes());
        return ResponseEntity.status(201).body(dtos.dto(res.valor(), res.alertas()));
    }
}
