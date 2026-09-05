package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.PositionRequestDTO;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreatePositionCommandHandler
        implements CommandHandler<CreatePositionCommand, ResponseEntity<Map<String, ?>>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreatePositionCommandHandler.class);

    private final PositionRepository positionRepository;

    @IgrpCommandHandler
    public ResponseEntity<Map<String, ?>> handle(CreatePositionCommand command) {
        PositionRequestDTO dto = command.getRequest();

        if (dto.getNumeroLugar() == null || dto.getNumeroLugar().isBlank())
            throw IgrpResponseStatusException.badRequest("O número do lugar é obrigatório.");
        if (dto.getJobId() == null || dto.getJobId().isBlank())
            throw IgrpResponseStatusException.badRequest("O cargo (jobId) é obrigatório.");
        if (dto.getUnidadeOrganicaId() == null || dto.getUnidadeOrganicaId().isBlank())
            throw IgrpResponseStatusException.badRequest("A unidade orgânica é obrigatória.");

        if (positionRepository.existsByNumeroLugar(dto.getNumeroLugar()))
            throw IgrpResponseStatusException.conflict(
                    "Já existe um Lugar com o número '" + dto.getNumeroLugar() + "'.");

        Position saved = positionRepository.save(Position.criar(
                dto.getNumeroLugar(),
                parse(dto.getJobId()),
                parse(dto.getUnidadeOrganicaId()),
                parse(dto.getCareerId()),
                parse(dto.getCategoryId()),
                parse(dto.getParentPositionId()),
                parse(dto.getManagesUnitId()),
                dto.getLegalBase()));

        return ResponseEntity.status(201).body(Map.of(
                "id", saved.getId().getStringValor(),
                "message", "Lugar criado com sucesso"));
    }

    private static UUID parse(String v) {
        return (v == null || v.isBlank()) ? null : UUID.fromString(v);
    }
}
