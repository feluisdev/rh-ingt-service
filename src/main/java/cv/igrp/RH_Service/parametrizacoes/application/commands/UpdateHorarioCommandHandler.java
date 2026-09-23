package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;
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
public class UpdateHorarioCommandHandler implements CommandHandler<UpdateHorarioCommand, ResponseEntity<HorarioResponseDTO>> {

    private final HorarioRepository horarioRepository;
    private final HorarioMapper horarioMapper;

    @IgrpCommandHandler
    public ResponseEntity<HorarioResponseDTO> handle(UpdateHorarioCommand command) {
        var dto = command.getHorarioRequest();
        Horario horario = horarioRepository.findById(HorarioId.from(command.getHorarioId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Horário não encontrado: " + command.getHorarioId()));

        // O que o pedido omitir fica como estava; o período de aferição e a duração diária limpam-se
        // em branco. Passar a FIXO sem os enviar limpa-os: são só do flexível.
        ControloHorario controlo = dto.getControlo() != null ? horarioMapper.controlo(dto.getControlo()) : horario.getControlo();
        boolean fixo = controlo == ControloHorario.FIXO;
        PeriodoAfericao periodo = dto.getPeriodoAfericao() != null ? horarioMapper.periodo(dto.getPeriodoAfericao())
                : fixo ? null : horario.getPeriodoAfericao();
        Integer duracao = dto.getDuracaoDiaria() != null ? horarioMapper.minutos(dto.getDuracaoDiaria())
                : fixo ? null : horario.getDuracaoDiariaMinutos();

        horario.atualizar(
                dto.getNome() != null ? dto.getNome() : horario.getNome(),
                controlo, periodo, duracao,
                dto.getBlocos() != null ? horarioMapper.blocos(dto.getBlocos()) : horario.getBlocos());

        return ResponseEntity.ok(horarioMapper.toDTO(horarioRepository.save(horario)));
    }
}
