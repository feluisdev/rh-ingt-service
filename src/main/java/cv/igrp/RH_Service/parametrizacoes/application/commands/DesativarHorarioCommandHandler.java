package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Desactivar não retira o horário a quem já o tem, nem à unidade: impede só que seja atribuído de
 * novo. As atribuições em curso mudam-se com uma atribuição nova.
 */
@Component
@RequiredArgsConstructor
public class DesativarHorarioCommandHandler implements CommandHandler<DesativarHorarioCommand, ResponseEntity<SuccessResponseDTO>> {

    private final HorarioRepository horarioRepository;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(DesativarHorarioCommand command) {
        var horario = horarioRepository.findById(HorarioId.from(command.getHorarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Horário não encontrado: " + command.getHorarioId()));
        horario.desativar();
        horarioRepository.save(horario);
        return ResponseEntity.ok(SuccessResponseDTO.de(command.getHorarioId()));
    }
}
