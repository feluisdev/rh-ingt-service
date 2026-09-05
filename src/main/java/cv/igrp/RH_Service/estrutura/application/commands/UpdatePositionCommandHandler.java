package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.PositionRequestDTO;
import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UpdatePositionCommandHandler
        implements CommandHandler<UpdatePositionCommand, ResponseEntity<PositionResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdatePositionCommandHandler.class);

    private final PositionRepository positionRepository;
    private final PositionMapper mapper;

    @IgrpCommandHandler
    public ResponseEntity<PositionResponseDTO> handle(UpdatePositionCommand command) {
        PositionRequestDTO dto = command.getRequest();

        Position position = positionRepository.findById(PositionId.from(command.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + command.getPositionId()));

        position.atualizar(
                parse(dto.getJobId()),
                parse(dto.getUnidadeOrganicaId()),
                parse(dto.getCareerId()),
                parse(dto.getCategoryId()),
                parse(dto.getParentPositionId()),
                parse(dto.getManagesUnitId()),
                dto.getLegalBase());

        Position saved = positionRepository.save(position);
        return ResponseEntity.ok(mapper.toDTO(saved));
    }

    private static UUID parse(String v) {
        return (v == null || v.isBlank()) ? null : UUID.fromString(v);
    }
}
