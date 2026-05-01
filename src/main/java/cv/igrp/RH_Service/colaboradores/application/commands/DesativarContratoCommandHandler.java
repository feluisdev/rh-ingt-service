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

@Component("colabsDesativarContratoCommandHandler")
@RequiredArgsConstructor
public class DesativarContratoCommandHandler
        implements CommandHandler<DesativarContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(DesativarContratoCommand command) {
        var id = ContratoId.from(command.getContratoId());
        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + command.getContratoId()));

        if (Boolean.FALSE.equals(contrato.getIsActive()))
            throw IgrpResponseStatusException.badRequest("O contrato já está inactivo.");

        // Block if it's the only active contract
        long activeCount = contratoRepository.countActiveByFuncionarioId(contrato.getFuncionarioId());
        if (activeCount <= 1)
            throw IgrpResponseStatusException.conflict("Não é possível desactivar o único contrato activo do funcionário.");

        contrato.desativar();
        contratoRepository.save(contrato);
        return ResponseEntity.ok(Map.of("message", "Desactivado com sucesso"));
    }
}
