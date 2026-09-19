package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveType;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveTypeRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveTypeId;
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
public class AtivarLeaveTypeCommandHandler implements CommandHandler<AtivarLeaveTypeCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AtivarLeaveTypeCommandHandler.class);

    private final LeaveTypeRepository leaveTypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(AtivarLeaveTypeCommand command) {
        var id = LeaveTypeId.from(java.util.UUID.fromString(command.getLeaveTypeId()));

        LeaveType leaveType = leaveTypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Tipo de licença não encontrado: " + command.getLeaveTypeId()));

        leaveType.reativar();
        leaveTypeRepository.save(leaveType);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getLeaveTypeId()));
    }
}
