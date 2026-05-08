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

@Component("colabsReativarContratoCommandHandler")
@RequiredArgsConstructor
public class ReativarContratoCommandHandler
        implements CommandHandler<ReativarContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(ReativarContratoCommand command) {
        var id = ContratoId.from(command.getContratoId());
        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Contrato não encontrado: " + command.getContratoId()));

        contrato.reativar();
        contratoRepository.save(contrato);
        return ResponseEntity.ok(Map.of("message", "Contrato reactivado com sucesso."));
    }
}
