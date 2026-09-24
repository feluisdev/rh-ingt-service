package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.HorarioColaborador;
import cv.igrp.RH_Service.colaboradores.domain.models.RegimePrestacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.HorarioColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.estrutura.domain.models.OrganizationalUnit;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <b>O horário de um colaborador</b> — assiduidade, primeiro passo (Lei n.º 20/X/2023, arts. 164.º a
 * 166.º). O horário que vale numa data é, por esta ordem:
 *
 * <ol>
 *   <li>o <b>atribuído</b> ao colaborador para essa data;</li>
 *   <li>o da <b>unidade</b> onde exerce funções nessa data, ou o da unidade-mãe mais próxima que
 *       tenha um (em regime presencial);</li>
 *   <li>o horário <b>base</b> da instituição (em regime presencial);</li>
 *   <li>nenhum — só enquanto a instituição não tiver marcado o base.</li>
 * </ol>
 *
 * <p>Um horário desactivado depois de atribuído continua a valer para quem o tem: desactivar só
 * impede atribuições novas.
 */
@Service
@RequiredArgsConstructor
public class HorarioColaboradorService implements cv.igrp.RH_Service.parametrizacoes.application.port.HorarioUtilizacaoPort {

    public enum Origem { COLABORADOR, UNIDADE, BASE, NENHUM }

    public record Vigente(Origem origem, Horario horario, RegimePrestacao regime, HorarioColaborador atribuicao) {}

    public record Resultado(HorarioColaborador atribuicao, List<String> alertas) {}

    private final HorarioColaboradorRepository horarioColaboradorRepository;
    private final HorarioRepository horarioRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final UnidadeDeExercicioService unidadeDeExercicio;
    private final cv.igrp.RH_Service.estrutura.application.services.HorarioDaUnidadeService horarioDaUnidadeService;
    private final cv.igrp.RH_Service.parametrizacoes.application.services.HorarioBaseService horarioBaseService;

    @Transactional
    public Resultado atribuir(FuncionarioId funcionarioId, String horarioId, RegimePrestacao regime, LocalDate dataInicio) {
        funcionario(funcionarioId);
        if (horarioId == null || horarioId.isBlank()) throw invalido("O horário é obrigatório.");
        HorarioId id;
        try {
            id = HorarioId.from(horarioId.trim());
        } catch (IllegalArgumentException e) {
            throw invalido("horarioId inválido: " + horarioId + ".");
        }
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> invalido("Horário não encontrado: " + horarioId + "."));
        if (!horario.isActive()) throw invalido("O horário '" + horario.getNome() + "' está inactivo.");

        List<HorarioColaborador> existentes = new ArrayList<>(horarioColaboradorRepository.findByFuncionario(funcionarioId));
        var nova = HorarioColaborador.atribuir(funcionarioId, id, regime, dataInicio, existentes);
        // A que estava aberta fechou na véspera: grava-se antes da nova.
        existentes.stream().filter(h -> dataInicio.minusDays(1).equals(h.getDataFim()))
                .forEach(horarioColaboradorRepository::save);
        var gravada = horarioColaboradorRepository.save(nova);

        return new Resultado(gravada, alertas(funcionarioId, horario, dataInicio));
    }

    @Transactional(readOnly = true)
    public List<HorarioColaborador> historico(FuncionarioId funcionarioId) {
        funcionario(funcionarioId);
        return horarioColaboradorRepository.findByFuncionario(funcionarioId);
    }

    @Transactional(readOnly = true)
    public Vigente vigente(FuncionarioId funcionarioId, LocalDate data) {
        funcionario(funcionarioId);
        var atribuida = horarioColaboradorRepository.findVigente(funcionarioId, data);
        if (atribuida.isPresent()) {
            Horario horario = horarioRepository.findById(atribuida.get().getHorarioId()).orElse(null);
            if (horario != null)
                return new Vigente(Origem.COLABORADOR, horario, atribuida.get().getRegimePrestacao(), atribuida.get());
        }
        return semAtribuicao(funcionarioId, data);
    }

    /** O que valeria se a pessoa não tivesse horário próprio: o da unidade, ou o base. */
    private Vigente semAtribuicao(FuncionarioId funcionarioId, LocalDate data) {
        UUID unidade = unidadeDeExercicio.unidadeOndeExerceFuncoes(funcionarioId, data);
        // O horário da unidade e o base também são os dessa data (têm histórico com data de efeito).
        HorarioId daUnidade = unidadeDeExercicio.herdado(unidade, u -> horarioDaUnidadeService.horarioEm(u, data));
        if (daUnidade != null) {
            var horario = horarioRepository.findById(daUnidade);
            if (horario.isPresent()) return new Vigente(Origem.UNIDADE, horario.get(), RegimePrestacao.PRESENCIAL, null);
        }
        return horarioBaseService.baseEm(data)
                .map(base -> new Vigente(Origem.BASE, base, RegimePrestacao.PRESENCIAL, null))
                .orElse(new Vigente(Origem.NENHUM, null, null, null));
    }

    /**
     * Tempo parcial no contrato com um horário de tantas ou mais horas do que o que a pessoa teria
     * sem ele (o da unidade, ou o base): alerta, não recusa — a percentagem pode estar noutro sítio.
     */
    private List<String> alertas(FuncionarioId funcionarioId, Horario horario, LocalDate data) {
        List<String> alertas = new ArrayList<>();
        boolean parcial = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .map(c -> RegimeTrabalho.TEMPO_PARCIAL.getCode().equals(c.getRegimeTrabalho()))
                .orElse(false);
        if (!parcial) return alertas;
        Vigente referencia = semAtribuicao(funcionarioId, data);
        if (referencia.horario() != null && horario.minutosSemanais() >= referencia.horario().minutosSemanais())
            alertas.add("O contrato é a tempo parcial, e o horário '" + horario.getNome()
                    + "' não tem menos horas do que o da " + (referencia.origem() == Origem.UNIDADE ? "unidade" : "instituição")
                    + " ('" + referencia.horario().getNome() + "').");
        return alertas;
    }

    private void funcionario(FuncionarioId funcionarioId) {
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + funcionarioId.getStringValor()));
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }

    /** Porta do catálogo: o horário já vigorou para algum colaborador (atribuição que começou antes da data). */
    @Override
    @Transactional(readOnly = true)
    public boolean vigorouAntesDe(HorarioId horarioId, LocalDate data) {
        return horarioColaboradorRepository.vigorouAntesDe(horarioId, data);
    }
}
