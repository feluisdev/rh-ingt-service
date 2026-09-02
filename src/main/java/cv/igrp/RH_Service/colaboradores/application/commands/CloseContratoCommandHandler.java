package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
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
    private final AssignmentService assignmentService;

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

        // Encerrar a afectação corrente na mesma data de cessação (modelo Position)
        var funcionarioId = FuncionarioId.from(contrato.getFuncionarioId().getValor());
        assignmentService.encerrarAfectacaoCorrente(funcionarioId, command.getEndDate());

        return ResponseEntity.ok(Map.of("message", "Contrato encerrado com sucesso."));
    }
}
