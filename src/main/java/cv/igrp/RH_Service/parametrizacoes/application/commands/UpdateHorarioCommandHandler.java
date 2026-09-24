package cv.igrp.RH_Service.parametrizacoes.application.commands;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.port.HorarioUtilizacaoPort;
import cv.igrp.RH_Service.parametrizacoes.application.services.HorarioBaseService;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
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
    private final HorarioBaseService horarioBaseService;
    /** Quem atribui horários (colaborador, unidade, base) diz se este já vigorou. */
    private final java.util.List<HorarioUtilizacaoPort> utilizacoes;

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

        java.util.List<BlocoHorario> blocos = dto.getBlocos() != null ? horarioMapper.blocos(dto.getBlocos()) : horario.getBlocos();

        // Um horário que já vigorou num dia passado não muda: o apuramento desses dias faz-se contra
        // ele (DL n.º 3/2010, art. 13.º). Só o nome muda; para outro conteúdo, duplica-se.
        if (!horario.mesmoConteudo(controlo, periodo, duracao, blocos) && jaVigorou(horario))
            throw IgrpResponseStatusException.conflict("O horário '" + horario.getNome() + "' já vigorou: os dias passados "
                    + "apuram-se contra ele, por isso os blocos, o controlo, a aferição e a duração não mudam. "
                    + "Duplique-o (POST /catalogs/horarios/{id}/duplicar) e atribua o novo a partir de uma data.");

        horario.atualizar(dto.getNome() != null ? dto.getNome() : horario.getNome(), controlo, periodo, duracao, blocos);

        return ResponseEntity.ok(horarioBaseService.comBaseDeHoje(horarioMapper.toDTO(horarioRepository.save(horario))));
    }

    private boolean jaVigorou(Horario horario) {
        var hoje = hoje();
        return utilizacoes.stream().anyMatch(u -> u.vigorouAntesDe(horario.getId(), hoje));
    }

    protected java.time.LocalDate hoje() { return java.time.LocalDate.now(); }
}
