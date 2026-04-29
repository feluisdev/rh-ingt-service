package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveTypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveTypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateLeaveTypeCommandHandler implements CommandHandler<UpdateLeaveTypeCommand, ResponseEntity<LeaveTypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLeaveTypeCommandHandler.class);

    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveTypeMapper leaveTypeMapper;

    @IgrpCommandHandler
    public ResponseEntity<LeaveTypeResponseDTO> handle(UpdateLeaveTypeCommand command) {
        var dto = command.getLeaveTypeRequest();
        var id = ExternalID.from(java.util.UUID.fromString(command.getLeaveTypeId()));

        LeaveType leaveType = leaveTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de licença não encontrado: " + command.getLeaveTypeId()));

        leaveType.atualizar(dto.getDescription(), dto.isDeductsBalance(), dto.isRequiresApproval(),
                dto.getMaxDaysPerYear(), dto.getCategoryOptionId());

        LeaveType updated = leaveTypeRepository.save(leaveType);

        LOGGER.debug("LeaveType actualizado: {}", updated.getId().getStringValor());

        return ResponseEntity.ok(leaveTypeMapper.toDTO(updated));
    }
}
