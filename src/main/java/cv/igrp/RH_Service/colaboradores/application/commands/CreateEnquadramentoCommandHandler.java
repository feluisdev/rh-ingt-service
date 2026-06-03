package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.EnquadramentoService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateEnquadramentoCommandHandler
        implements CommandHandler<CreateEnquadramentoCommand, ResponseEntity<Map<String, ?>>> {

    private final EnquadramentoService enquadramentoService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(CreateEnquadramentoCommand command) {
        if (command.getRequest().getDataInicio() == null)
            throw IgrpResponseStatusException.badRequest("A data de início é obrigatória.");

        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());
        var saved = enquadramentoService.criarEnquadramento(funcionarioId, command.getRequest());
        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Criado com sucesso"));
    }
}
