package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
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

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;

    @IgrpCommandHandler
    @Transactional
    public ResponseEntity<HorarioResponseDTO> handle(MarcarHorarioBaseCommand command) {
        var id = HorarioId.from(command.getHorarioId());
        var horario = horarioRepository.findById(id)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Horário não encontrado: " + command.getHorarioId()));
        horario.marcarComoBase();

        horarioRepository.findBase()
                .filter(anterior -> !anterior.getId().equals(id))
                .ifPresent(anterior -> {
                    anterior.desmarcarBase();
                    horarioRepository.save(anterior);
                });

        return ResponseEntity.ok(horarioMapper.toDTO(horarioRepository.save(horario)));
    }
}
