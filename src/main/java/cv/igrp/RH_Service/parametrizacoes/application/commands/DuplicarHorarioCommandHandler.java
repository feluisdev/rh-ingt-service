package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.services.HorarioBaseService;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DuplicarHorarioCommandHandler implements CommandHandler<DuplicarHorarioCommand, ResponseEntity<HorarioResponseDTO>> {

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;
    private final HorarioBaseService horarioBaseService;

    @IgrpCommandHandler
    public ResponseEntity<HorarioResponseDTO> handle(DuplicarHorarioCommand command) {
        Horario original = horarioRepository.findById(HorarioId.from(command.getHorarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Horário não encontrado: " + command.getHorarioId()));
        String nome = command.getNome() != null && !command.getNome().isBlank() ? command.getNome().trim()
                : original.getNome() + " (cópia)";
        Horario copia = Horario.criar(nome, original.getControlo(), original.getPeriodoAfericao(),
                original.getDuracaoDiariaMinutos(), original.getBlocos());
        return ResponseEntity.status(201).body(horarioBaseService.comBaseDeHoje(horarioMapper.toDTO(horarioRepository.save(copia))));
    }
}
