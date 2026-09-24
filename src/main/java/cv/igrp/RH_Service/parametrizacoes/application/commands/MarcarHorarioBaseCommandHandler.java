package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.services.HorarioBaseService;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * O horário da instituição: vale para quem não tem horário na pessoa nem em nenhuma unidade da
 * cadeia. Há no máximo um — marcar outro desmarca o anterior.
 */
@Component
@RequiredArgsConstructor
public class MarcarHorarioBaseCommandHandler implements CommandHandler<MarcarHorarioBaseCommand, ResponseEntity<HorarioResponseDTO>> {

    private final HorarioMapper horarioMapper;
    private final HorarioBaseService horarioBaseService;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<HorarioResponseDTO> handle(MarcarHorarioBaseCommand command) {
        var horario = horarioBaseService.marcar(HorarioId.from(command.getHorarioId()), command.getDesde());
        return ResponseEntity.ok(horarioBaseService.comBaseDeHoje(horarioMapper.toDTO(horario)));
    }
}
