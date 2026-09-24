package cv.igrp.RH_Service.estrutura.application.commands;

import cv.igrp.RH_Service.estrutura.application.dto.EstadoLugarRequestDTO;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Descongelar um Lugar: volta a {@code ATIVO} e à dotação, e pode voltar a ser ocupado
 * (BR-POS-07). Exige motivo, como congelar. Um Lugar extinto não volta (409).
 */
@Component
@RequiredArgsConstructor
public class DescongelarPositionCommandHandler
        implements CommandHandler<DescongelarPositionCommand, ResponseEntity<SuccessResponseDTO>> {

    private final PositionRepository positionRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DescongelarPositionCommand command) {
        EstadoLugarRequestDTO req = command.getRequest();
        if (req == null || req.getMotivo() == null || req.getMotivo().isBlank()) {
            throw IgrpResponseStatusException.badRequest("Indique o motivo para descongelar o Lugar.");
        }
        Position position = positionRepository.findById(PositionId.from(command.getPositionId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("O Lugar indicado não existe."));

        if (!position.descongelar(req.getMotivo(), req.getDespachoNumero(), LocalDate.now())) {
            return ResponseEntity.ok(SuccessResponseDTO.semEfeito(command.getPositionId(), "O Lugar já está activo."));
        }
        positionRepository.save(position);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getPositionId()));
    }
}
