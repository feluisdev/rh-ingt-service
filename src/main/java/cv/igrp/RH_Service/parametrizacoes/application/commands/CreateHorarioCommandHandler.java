package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.RH_Service.shared.application.dto.SuccessResponseDTO;
import cv.igrp.framework.core.domain.CommandHandler;
import cv.igrp.framework.stereotype.IgrpCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateHorarioCommandHandler implements CommandHandler<CreateHorarioCommand, ResponseEntity<SuccessResponseDTO>> {

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;

    @IgrpCommandHandler
    public ResponseEntity<SuccessResponseDTO> handle(CreateHorarioCommand command) {
        var dto = command.getHorarioRequest();
        var horario = Horario.criar(dto.getNome(), horarioMapper.controlo(dto.getControlo()),
                horarioMapper.periodo(dto.getPeriodoAfericao()), horarioMapper.minutos(dto.getDuracaoDiaria()),
                horarioMapper.blocos(dto.getBlocos()));
        var saved = horarioRepository.save(horario);
        return ResponseEntity.status(201).body(SuccessResponseDTO.de(saved.getId().getStringValor()));
    }
}
