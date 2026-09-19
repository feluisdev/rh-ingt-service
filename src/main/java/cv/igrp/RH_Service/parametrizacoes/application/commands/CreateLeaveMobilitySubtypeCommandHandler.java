package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.LeaveMobilitySubtype;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.LeaveMobilitySubtypeRepository;
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
public class CreateLeaveMobilitySubtypeCommandHandler implements CommandHandler<CreateLeaveMobilitySubtypeCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateLeaveMobilitySubtypeCommandHandler.class);

    private final LeaveMobilitySubtypeRepository leaveMobilitySubtypeRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateLeaveMobilitySubtypeCommand command) {
        var dto = command.getLeaveMobilitySubtypeRequest();

        if (leaveMobilitySubtypeRepository.existsByCode(dto.getCode())) {
            throw IgrpResponseStatusException.conflict(
                "Já existe um subtipo de licença/mobilidade com o código: '" + dto.getCode() + "'.");
        }

        LeaveMobilitySubtype leaveMobilitySubtype = LeaveMobilitySubtype.criar(
            dto.getCode(),
            dto.getDescription(),
            dto.getRecordType(),
            dto.isAffectsPay(),
            dto.isCountsForSeniority(),
            dto.isCanSelfSubmit(),
            dto.getMaxDurationDays(),
            dto.getMaxExtensions(),
            dto.getPositionEffect(),
            dto.getVacancyAfterDays(),
            dto.getReturnEffect()
        );

        LeaveMobilitySubtype saved = leaveMobilitySubtypeRepository.save(leaveMobilitySubtype);

        LOGGER.debug("LeaveMobilitySubtype criado com id: {}", saved.getId().getStringValor());

        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
