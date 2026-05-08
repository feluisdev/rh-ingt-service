package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsAtualizarProcessoDisciplinarCommandHandler")
@RequiredArgsConstructor
public class AtualizarProcessoDisciplinarCommandHandler
        implements CommandHandler<AtualizarProcessoDisciplinarCommand, ResponseEntity<Map<String, ?>>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ProcessoDisciplinarRepository processoDisciplinarRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(AtualizarProcessoDisciplinarCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível actualizar processo disciplinar de funcionário inactivo.");

        var processo = processoDisciplinarRepository.findById(ProcessoDisciplinarId.from(command.getProcessoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Processo disciplinar não encontrado: " + command.getProcessoId()));

        if (!processo.getFuncionarioId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound(
                    "Processo disciplinar não encontrado: " + command.getProcessoId());

        processo.atualizar(dto.getProcessNumber(), dto.getStartDate(), dto.getEndDate(),
                dto.getPenalty(), dto.getPenaltyStartDate(), dto.getPenaltyEndDate(),
                dto.getOfficialBulletin(), dto.getNotes());

        processoDisciplinarRepository.save(processo);
        return ResponseEntity.ok(Map.of("message", "Processo disciplinar actualizado com sucesso"));
    }
}
