package cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers;

import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioBlocoDTO;
import cv.igrp.RH_Service.parametrizacoes.application.dto.HorarioResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.ControloHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.PeriodoAfericao;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.regex.Pattern;

/** Entre o pedido (texto: {@code HH:mm}, dia 1..7, enums por nome) e o domínio. */
@Component
public class HorarioMapper {

    private static final Pattern HH_MM = Pattern.compile("\\d{2}:\\d{2}");

    public HorarioResponseDTO toDTO(Horario h) {
        if (h == null) return null;
        var dto = new HorarioResponseDTO();
        dto.setId(h.getId().getStringValor());
        dto.setNome(h.getNome());
        dto.setControlo(h.getControlo().name());
        dto.setPeriodoAfericao(h.getPeriodoAfericao() != null ? h.getPeriodoAfericao().name() : null);
        dto.setDuracaoDiaria(h.getDuracaoDiariaMinutos() != null ? duracao(h.getDuracaoDiariaMinutos()) : null);
        dto.setBlocos(h.getBlocos().stream().map(b -> new HorarioBlocoDTO(b.dia().getValue(),
                b.inicio().toString(), b.fim().toString(), b.obrigatorio())).toList());
        dto.setHorasSemanais(duracao(h.minutosSemanais()));
        dto.setIsBase(h.isBase());
        dto.setIsActive(h.isActive());
        dto.setEstadoDesc(h.isActive() ? "Ativo" : "Inativo");
        return dto;
    }

    public ControloHorario controlo(String valor) {
        if (valor == null) return null;
        try {
            return ControloHorario.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            throw invalido("controlo inválido: '" + valor + "'. Valores: FIXO, FLEXIVEL.");
        }
    }

    /** Nulo ou em branco = sem período. */
    public PeriodoAfericao periodo(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return PeriodoAfericao.valueOf(valor.trim());
        } catch (IllegalArgumentException e) {
            throw invalido("periodoAfericao inválido: '" + valor + "'. Valores: SEMANA, MES.");
        }
    }

    /** {@code HH:mm} em minutos; nulo ou em branco = sem duração. */
    public Integer minutos(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        String v = hhmm.trim();
        if (!HH_MM.matcher(v).matches())
            throw invalido("duracaoDiaria escreve-se HH:mm (ex.: 07:00).");
        int h = Integer.parseInt(v.substring(0, 2));
        int m = Integer.parseInt(v.substring(3));
        if (m > 59) throw invalido("duracaoDiaria com minutos inválidos: " + hhmm + ".");
        return h * 60 + m;
    }

    public List<BlocoHorario> blocos(List<HorarioBlocoDTO> dtos) {
        if (dtos == null) return null;
        return dtos.stream().map(this::bloco).toList();
    }

    private BlocoHorario bloco(HorarioBlocoDTO d) {
        if (d == null || d.getDiaSemana() == null || d.getInicio() == null || d.getFim() == null)
            throw invalido("Cada bloco tem diaSemana, inicio e fim.");
        DayOfWeek dia;
        try {
            dia = DayOfWeek.of(d.getDiaSemana());
        } catch (DateTimeException e) {
            throw invalido("diaSemana vai de 1 (segunda) a 7 (domingo): " + d.getDiaSemana() + ".");
        }
        return new BlocoHorario(dia, hora(d.getInicio()), hora(d.getFim()), !Boolean.FALSE.equals(d.getObrigatorio()));
    }

    private static LocalTime hora(String hhmm) {
        if (!HH_MM.matcher(hhmm.trim()).matches())
            throw invalido("As horas escrevem-se HH:mm (ex.: 08:30): " + hhmm + ".");
        try {
            return LocalTime.parse(hhmm.trim());
        } catch (DateTimeException e) {
            throw invalido("Hora inválida: " + hhmm + ".");
        }
    }

    private static String duracao(int minutos) {
        return String.format("%02d:%02d", minutos / 60, minutos % 60);
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
