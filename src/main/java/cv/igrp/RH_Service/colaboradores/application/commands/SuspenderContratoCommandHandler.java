package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsSuspenderContratoCommandHandler")
@RequiredArgsConstructor
public class SuspenderContratoCommandHandler
        implements CommandHandler<SuspenderContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(SuspenderContratoCommand command) {
        var id = ContratoId.from(command.getContratoId());
        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Contrato não encontrado: " + command.getContratoId()));

        contrato.suspender();
        contratoRepository.save(contrato);
        return ResponseEntity.ok(Map.of("message", "Contrato suspenso com sucesso."));
    }
}
