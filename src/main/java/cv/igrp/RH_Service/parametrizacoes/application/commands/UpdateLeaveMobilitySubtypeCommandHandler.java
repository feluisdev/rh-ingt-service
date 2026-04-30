package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.LeaveMobilitySubtypeResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.LeaveMobilitySubtypeMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.LeaveMobilitySubtypeId;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateLeaveMobilitySubtypeCommandHandler implements CommandHandler<UpdateLeaveMobilitySubtypeCommand, ResponseEntity<LeaveMobilitySubtypeResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLeaveMobilitySubtypeCommandHandler.class);

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;
    private final LeaveMobilitySubtypeMapper leaveMobilitySubtypeMapper;

    @IgrpCommandHandler
    public ResponseEntity<LeaveMobilitySubtypeResponseDTO> handle(UpdateLeaveMobilitySubtypeCommand command) {
        var dto = command.getLeaveMobilitySubtypeRequest();
        var id = LeaveMobilitySubtypeId.from(java.util.UUID.fromString(command.getLeaveMobilitySubtypeId()));

        LeaveMobilitySubtype leaveMobilitySubtype = leaveMobilitySubtypeRepository.findById(id)
            .orElseThrow(() -> IgrpResponseStatusException.notFound(
                "Subtipo de licença/mobilidade não encontrado: " + command.getLeaveMobilitySubtypeId()));

        leaveMobilitySubtype.atualizar(dto.getDescription(), dto.isAffectsPay(),
                dto.isCountsForSeniority(), dto.isCanSelfSubmit());

        LeaveMobilitySubtype updated = leaveMobilitySubtypeRepository.save(leaveMobilitySubtype);

        LOGGER.debug("LeaveMobilitySubtype actualizado: {}", updated.getId().getStringValor());

        return ResponseEntity.ok(leaveMobilitySubtypeMapper.toDTO(updated));
    }
}
