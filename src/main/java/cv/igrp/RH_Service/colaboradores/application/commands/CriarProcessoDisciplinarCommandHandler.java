package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProcessoDisciplinarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component("colabsCriarProcessoDisciplinarCommandHandler")
@RequiredArgsConstructor
public class CriarProcessoDisciplinarCommandHandler
        implements CommandHandler<CriarProcessoDisciplinarCommand, ResponseEntity<SuccessResponseDTO>> {

    private final FuncionarioRepository funcionarioRepository;
    private final ProcessoDisciplinarRepository processoDisciplinarRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CriarProcessoDisciplinarCommand command) {
        var dto = command.getRequest();
        var funcionarioId = FuncionarioId.from(command.getFuncionarioId());

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + command.getFuncionarioId()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.badRequest(
                    "Não é possível registar processo disciplinar para funcionário inactivo.");

        var saved = processoDisciplinarRepository.save(ProcessoDisciplinar.criar(
                funcionarioId, dto.getProcessNumber(), dto.getStartDate(), dto.getEndDate(),
                dto.getPenalty(), dto.getPenaltyStartDate(), dto.getPenaltyEndDate(),
                dto.getOfficialBulletin(), dto.getNotes()));

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
