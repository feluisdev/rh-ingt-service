package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.HorarioColaboradorId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * O horário atribuído a um colaborador, com o regime de prestação, por um período (assiduidade,
 * primeiro passo). É um histórico sem sobreposição: uma atribuição nova fecha a anterior na véspera.
 * Sem atribuição em vigor, vale o horário da unidade onde exerce funções, ou o horário base.
 */
@Getter
public class HorarioColaborador {

    private HorarioColaboradorId id;
    private FuncionarioId funcionarioId;
    private HorarioId horarioId;
    private RegimePrestacao regimePrestacao;
    private LocalDate dataInicio;
    /** Nula enquanto vigorar. */
    private LocalDate dataFim;

    private HorarioColaborador() {}

    /**
     * A atribuição seguinte às que já existem. Só se acrescenta ao fim do histórico: a que estiver
     * aberta fecha na véspera do início desta; uma que comece no mesmo dia ou depois recusa-a.
     */
    public static HorarioColaborador atribuir(FuncionarioId funcionarioId, HorarioId horarioId,
                                              RegimePrestacao regime, LocalDate dataInicio,
                                              List<HorarioColaborador> existentes) {
        Objects.requireNonNull(funcionarioId);
        Objects.requireNonNull(horarioId);
        if (dataInicio == null) throw invalido("A data de início é obrigatória.");

        for (HorarioColaborador h : existentes) {
            if (!h.dataInicio.isBefore(dataInicio))
                throw invalido("Já há um horário atribuído desde " + h.dataInicio
                        + ": uma atribuição nova começa depois da última.");
            if (h.dataFim != null && !h.dataFim.isBefore(dataInicio))
                throw invalido("A atribuição de " + h.dataInicio + " a " + h.dataFim
                        + " sobrepõe-se a esta.");
        }
        existentes.stream().filter(h -> h.dataFim == null).forEach(h -> h.dataFim = dataInicio.minusDays(1));

        var novo = new HorarioColaborador();
        novo.id = HorarioColaboradorId.gerarNovo();
        novo.funcionarioId = funcionarioId;
        novo.horarioId = horarioId;
        novo.regimePrestacao = regime != null ? regime : RegimePrestacao.PRESENCIAL;
        novo.dataInicio = dataInicio;
        return novo;
    }

    public static HorarioColaborador reconstruir(HorarioColaboradorId id, FuncionarioId funcionarioId,
                                                 HorarioId horarioId, RegimePrestacao regime,
                                                 LocalDate dataInicio, LocalDate dataFim) {
        var h = new HorarioColaborador();
        h.id = id;
        h.funcionarioId = funcionarioId;
        h.horarioId = horarioId;
        h.regimePrestacao = regime;
        h.dataInicio = dataInicio;
        h.dataFim = dataFim;
        return h;
    }

    public boolean vigoraEm(LocalDate data) {
        return !data.isBefore(dataInicio) && (dataFim == null || !data.isAfter(dataFim));
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
