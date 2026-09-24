package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.EstadoLugarRequestDTO;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Congelar um Lugar: sai da dotação e deixa de poder ser ocupado (BR-POS-06).
 *
 * <p>É um acto administrativo, por isso exige motivo; o despacho é opcional. Não se congela
 * um Lugar com titular — congelar é tirar uma vaga do quadro, e com alguém sentado não há
 * vaga a tirar: o titular sai primeiro (transferência, cessação). Um Lugar que não pode
 * voltar não se congela: extingue-se.
 */
@Component
@RequiredArgsConstructor
public class CongelarPositionCommandHandler
        implements CommandHandler<CongelarPositionCommand, ResponseEntity<SuccessResponseDTO>> {

    private final PositionRepository positionRepository;
    private final PositionOccupancyPort positionOccupancyPort;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CongelarPositionCommand command) {
        EstadoLugarRequestDTO req = command.getRequest();
        if (req == null || req.getMotivo() == null || req.getMotivo().isBlank()) {
            throw IgrpResponseStatusException.badRequest("Indique o motivo para congelar o Lugar.");
        }
        Position position = positionRepository.findById(PositionId.from(command.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("O Lugar indicado não existe."));

        if (Position.CONGELADO.equals(position.getEstado())) {
            return ResponseEntity.ok(SuccessResponseDTO.semEfeito(command.getPositionId(), "O Lugar já está congelado."));
        }
        if (!Position.EXTINTO.equals(position.getEstado())
                && positionOccupancyPort.ocupados(List.of(position.getId().getValor())).contains(position.getId().getValor())) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "O Lugar " + position.getNumeroLugar() + " está ocupado. Só se pode congelar um Lugar vago: "
                            + "primeiro, o titular tem de sair (por transferência ou cessação).");
        }

        position.congelar(req.getMotivo(), req.getDespachoNumero(), LocalDate.now());
        positionRepository.save(position);

        return ResponseEntity.ok(SuccessResponseDTO.de(command.getPositionId()));
    }
}
