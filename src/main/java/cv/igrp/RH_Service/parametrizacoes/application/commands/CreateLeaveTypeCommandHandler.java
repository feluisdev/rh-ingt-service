package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class CreateLeaveTypeCommandHandler implements CommandHandler<CreateLeaveTypeCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLeaveTypeCommandHandler.class);

    private final LeaveTypeRepository leaveTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateLeaveTypeCommand command) {
        var dto = command.getLeaveTypeRequest();

        if (leaveTypeRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um tipo de licença com o código: '" + dto.getCode() + "'.");
        }

        LeaveType leaveType = LeaveType.criar(
            dto.getCode(),
            dto.getDescription(),
            dto.isDeductsBalance(),
            dto.isRequiresApproval(),
            dto.getMaxDaysPerYear(),
            dto.getCategory()
        );

        LeaveType saved = leaveTypeRepository.save(leaveType);

        LOGGER.debug("LeaveType criado com id: {}", saved.getId().getStringValor());

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
