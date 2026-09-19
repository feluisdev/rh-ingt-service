package cv.igrp.RH_Service.colaboradores.application.commands;

import cv.igrp.RH_Service.colaboradores.application.dto.AfectacaoRequestDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AssignmentService;
import cv.igrp.RH_Service.colaboradores.domain.models.Assignment;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAfectacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AfectarColaboradorCommandHandler
        implements CommandHandler<AfectarColaboradorCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfectarColaboradorCommandHandler.class);

    private final AssignmentService assignmentService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<SuccessResponseDTO> handle(AfectarColaboradorCommand command) {
        AfectacaoRequestDTO dto = command.getRequest();

        if (dto.getFuncionarioId() == null || dto.getFuncionarioId().isBlank())
            throw IgrpResponseStatusException.badRequest("O funcionário é obrigatório.");
        if (dto.getPositionId() == null || dto.getPositionId().isBlank())
            throw IgrpResponseStatusException.badRequest("O Lugar (positionId) é obrigatório.");

        LocalDate dataInicio = dto.getDataInicio() != null ? dto.getDataInicio() : LocalDate.now();
        String origem = (dto.getOrigem() == null || dto.getOrigem().isBlank())
                ? Assignment.ADMISSAO : dto.getOrigem();

        Assignment saved = assignmentService.afectar(
                FuncionarioId.from(dto.getFuncionarioId()),
                UUID.fromString(dto.getPositionId()),
                parse(dto.getGradeId()),
                parse(dto.getFunctionId()),
                origem,
                TipoAfectacao.de(dto.getAssignmentType()),
                dataInicio,
                dto.getNotes());

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }

    private static UUID parse(String v) {
        return (v == null || v.isBlank()) ? null : UUID.fromString(v);
    }
}
