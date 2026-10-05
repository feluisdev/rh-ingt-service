package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ExtinguirPositionCommandHandler
        implements CommandHandler<ExtinguirPositionCommand, ResponseEntity<SuccessResponseDTO>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtinguirPositionCommandHandler.class);

    private final PositionRepository positionRepository;
    private final PositionOccupancyPort positionOccupancyPort;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(ExtinguirPositionCommand command) {
        Position position = positionRepository.findById(PositionId.from(command.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Lugar não encontrado: " + command.getPositionId()));

        // BR-AF-27: quem aguarda o contrato ficaria sem o Lugar que lhe foi reservado
        var reserva = positionOccupancyPort.reservados(List.of(position.getId().getValor())).get(position.getId().getValor());
        if (reserva != null)
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar " + position.getNumeroLugar() + " está reservado para "
                            + (reserva.nome() != null ? reserva.nome() : "um colaborador")
                            + ", que aguarda o contrato. Cancele primeiro a reserva para o poder extinguir.");

        position.extinguir();
        positionRepository.save(position);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getPositionId()));
    }
}
