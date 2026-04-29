package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CreateLeaveTypeCommandHandler implements CommandHandler<CreateLeaveTypeCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLeaveTypeCommandHandler.class);

    private final LeaveTypeRepository leaveTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreateLeaveTypeCommand command) {
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
            dto.getCategoryOptionId()
        );

        LeaveType saved = leaveTypeRepository.save(leaveType);

        LOGGER.debug("LeaveType criado com id: {}", saved.getId().getStringValor());

        return ResponseEntity.status(201).body(Map.of(
            "id", saved.getId().getStringValor(),
            "message", "Tipo de licença criado com sucesso"
        ));
    }
}
