package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.EnquadramentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component("colabsCloseContratoCommandHandler")
@RequiredArgsConstructor
public class CloseContratoCommandHandler
        implements CommandHandler<CloseContratoCommand, ResponseEntity<Map<String, ?>>> {

    private final ContratoRepository contratoRepository;
    private final EnquadramentoRepository enquadramentoRepository;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<Map<String, ?>> handle(CloseContratoCommand command) {
        if (command.getEndDate() == null)
            throw IgrpResponseStatusException.badRequest("O campo endDate é obrigatório.");
        if (command.getTerminationReason() == null || command.getTerminationReason().isBlank())
            throw IgrpResponseStatusException.badRequest("O campo terminationReason é obrigatório.");

        var id = ContratoId.from(command.getContratoId());
        var contrato = contratoRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Contrato não encontrado: " + command.getContratoId()));

        contrato.encerrar(command.getEndDate(), command.getTerminationReason());
        contratoRepository.save(contrato);

        // Encerrar enquadramento activo na mesma data de cessação
        var funcionarioId = FuncionarioId.from(contrato.getFuncionarioId().getValor());
        enquadramentoRepository.findCurrentByFuncionarioId(funcionarioId).ifPresent(enquadramento -> {
            enquadramento.encerrar(command.getEndDate());
            enquadramentoRepository.save(enquadramento);
        });

        return ResponseEntity.ok(Map.of("message", "Contrato encerrado com sucesso."));
    }
}
