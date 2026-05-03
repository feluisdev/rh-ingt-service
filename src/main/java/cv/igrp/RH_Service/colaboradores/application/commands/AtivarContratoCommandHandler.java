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

@Component("colabsAtivarContratoCommandHandler")
@RequiredArgsConstructor
public class AtivarContratoCommandHandler
        implements CommandHandler<AtivarContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtivarContratoCommand command) {
        var id = ContratoId.from(command.getContratoId());
        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + command.getContratoId()));

        if (Boolean.TRUE.equals(contrato.getIsActive()))
            return ResponseEntity.ok(Map.of("message", "Contrato já está activo."));

        if (contratoRepository.existsActiveByFuncionarioId(contrato.getFuncionarioId()))
            throw IgrpResponseStatusException.conflict("Já existe um contrato activo para este funcionário.");

        contrato.ativar();
        contratoRepository.save(contrato);
        return ResponseEntity.ok(Map.of("message", "Activado com sucesso"));
    }
}
