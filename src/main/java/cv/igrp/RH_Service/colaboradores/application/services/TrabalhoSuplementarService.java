package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.DiaAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.MarcacaoAssiduidade;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoDiaSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.models.TrabalhoSuplementar;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MarcacaoAssiduidadeRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.TrabalhoSuplementarRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TrabalhoSuplementarId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.BlocoHorario;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.shared.application.constants.RegimeTrabalho;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>Trabalho suplementar</b> (horas extras) — Lei n.º 20/X/2023, art. 155.º n.º 2 a). Autoriza-se um
 * intervalo de um dia; as horas realizadas saem das marcações; classifica-se pelo tipo de dia.
 *
 * <ul>
 *   <li>A chefia directa ou o RH <b>lançam</b> (nasce autorizado, também para um dia passado — o caso
 *       urgente, que fica assinalado); o próprio <b>pede</b>, para hoje ou para a frente, e a chefia
 *       directa ou o RH decidem.</li>
 *   <li>Quem tem <b>isenção de horário</b> não faz trabalho suplementar: a isenção já é um suplemento
 *       permanente (art. 155.º n.º 2 b)).</li>
 *   <li>Num dia útil, o intervalo fica <b>fora dos blocos do horário</b> — dentro é tempo normal.</li>
 * </ul>
 *
 * <p>Sem valores: o suplemento, o tecto de um terço da remuneração base (n.º 7) e os limites em horas
 * (diploma de desenvolvimento, art. 165.º n.º 4) ficam para o processamento salarial e para o jurídico.
 */
@Service
@RequiredArgsConstructor
public class TrabalhoSuplementarService {

    /** Um trabalho suplementar lido: o tipo de dia e as horas realizadas calculam-se. */
    public record Linha(TrabalhoSuplementar trabalho, TipoDiaSuplementar tipoDia, int minutosRealizados,
                        boolean semRegisto) {}

    public record Mes(YearMonth mes, List<Linha> linhas, Map<TipoDiaSuplementar, Integer> realizadosPorTipo,
                      int minutosAutorizados, int minutosRealizados) {}

    private final TrabalhoSuplementarRepository trabalhoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ContratoRepository contratoRepository;
    private final MarcacaoAssiduidadeRepository marcacaoRepository;
    private final CalendarioFeriadosService calendarioFeriadosService;
    private final HorarioColaboradorService horarioColaboradorService;
    private final ChefiaService chefiaService;

    /**
     * Lançado pela chefia directa ({@code chefe}) ou pelo RH ({@code chefe} nulo): nasce autorizado.
     * Ninguém lança o seu próprio; a chefia só lança para a sua equipa directa (403).
     */
    @Transactional
    public TrabalhoSuplementar lancar(FuncionarioId chefe, FuncionarioId funcionarioId, LocalDate data,
                                      LocalTime horaInicio, LocalTime horaFim, String motivo) {
        if (chefe != null) exigirChefiaDe(chefe, funcionarioId);
        var t = TrabalhoSuplementar.lancar(funcionarioId, data, horaInicio, horaFim, motivo, chefe, agora());
        validar(t);
        return trabalhoRepository.save(t);
    }

    /** Pedido pelo próprio: fica PEDIDO até a chefia directa ou o RH decidirem. */
    @Transactional
    public TrabalhoSuplementar pedir(FuncionarioId funcionarioId, LocalDate data, LocalTime horaInicio,
                                     LocalTime horaFim, String motivo) {
        var t = TrabalhoSuplementar.pedir(funcionarioId, data, horaInicio, horaFim, motivo, agora());
        validar(t);
        return trabalhoRepository.save(t);
    }

    /** Autorizar ou recusar um pedido. {@code chefe} nulo: é o RH, que decide sempre. */
    @Transactional
    public TrabalhoSuplementar decidir(FuncionarioId chefe, FuncionarioId funcionarioId, TrabalhoSuplementarId id,
                                       boolean autorizar, String motivo) {
        var t = encontrar(funcionarioId, id);
        if (chefe != null) exigirChefiaDe(chefe, t.getFuncionarioId());
        if (autorizar) t.autorizar(chefe, agora());
        else t.recusar(chefe, motivo, agora());
        return trabalhoRepository.save(t);
    }

    /** O RH cancela um pedido ou uma autorização que já não vale; fica, cancelado, com o motivo. */
    @Transactional
    public TrabalhoSuplementar cancelar(FuncionarioId funcionarioId, TrabalhoSuplementarId id, String motivo) {
        var t = encontrar(funcionarioId, id);
        t.cancelar(motivo, agora());
        return trabalhoRepository.save(t);
    }

    /** Os pedidos por decidir da equipa directa de uma chefia. */
    @Transactional(readOnly = true)
    public List<TrabalhoSuplementar> pendentesDaEquipa(FuncionarioId chefe) {
        return trabalhoRepository.findPedidosDe(chefiaService.equipaDirecta(chefe));
    }

    /** O trabalho suplementar de um mês: cada um com o tipo de dia e as horas realizadas; os totais dos autorizados. */
    @Transactional(readOnly = true)
    public Mes doMes(FuncionarioId funcionarioId, YearMonth mes) {
        funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + funcionarioId.getStringValor()));
        LocalDate de = mes.atDay(1);
        LocalDate ate = mes.atEndOfMonth();
        LocalDate hoje = agora().toLocalDate();
        List<TrabalhoSuplementar> trabalhos = trabalhoRepository.findByFuncionarioEntre(funcionarioId, de, ate);
        List<MarcacaoAssiduidade> marcacoes = trabalhos.isEmpty() ? List.of()
                : marcacaoRepository.findByFuncionarioEntre(funcionarioId, de, ate);
        Set<LocalDate> feriados = trabalhos.isEmpty() ? Set.of()
                : calendarioFeriadosService.feriadosDoColaborador(funcionarioId, de, ate);

        List<Linha> linhas = new ArrayList<>();
        Map<TipoDiaSuplementar, Integer> porTipo = new EnumMap<>(TipoDiaSuplementar.class);
        for (TipoDiaSuplementar tipo : TipoDiaSuplementar.values()) porTipo.put(tipo, 0);
        int autorizados = 0, realizados = 0;
        for (TrabalhoSuplementar t : trabalhos) {
            Horario horario = horarioColaboradorService.vigente(funcionarioId, t.getData()).horario();
            TipoDiaSuplementar tipo = tipoDia(t.getData(), horario, feriados);
            if (!t.isAutorizado()) {
                linhas.add(new Linha(t, tipo, 0, false));
                continue;
            }
            List<MarcacaoAssiduidade> doDia = marcacoes.stream()
                    .filter(m -> m.getMomento().toLocalDate().equals(t.getData())).toList();
            boolean semRegisto = t.getData().isBefore(hoje) && doDia.stream().noneMatch(MarcacaoAssiduidade::conta);
            int minutos = t.minutosRealizados(DiaAssiduidade.calcular(t.getData(), doDia).periodos(),
                    tipo == TipoDiaSuplementar.DIA_UTIL ? blocosDoDia(horario, t.getData()) : List.of());
            linhas.add(new Linha(t, tipo, minutos, semRegisto));
            autorizados += t.minutosAutorizados();
            realizados += minutos;
            if (tipo != null) porTipo.merge(tipo, minutos, Integer::sum);
        }
        return new Mes(mes, linhas, porTipo, autorizados, realizados);
    }

    /**
     * Os intervalos autorizados de cada dia, para o apuramento de faltas: num horário flexível, o que
     * é trabalho suplementar não conta outra vez para o saldo da aferição.
     */
    @Transactional(readOnly = true)
    public Map<LocalDate, List<DiaAssiduidade.Periodo>> autorizadosPorDia(FuncionarioId funcionarioId,
                                                                        LocalDate de, LocalDate ate) {
        Map<LocalDate, List<DiaAssiduidade.Periodo>> porDia = new HashMap<>();
        for (TrabalhoSuplementar t : trabalhoRepository.findByFuncionarioEntre(funcionarioId, de, ate))
            if (t.isAutorizado()) porDia.computeIfAbsent(t.getData(), k -> new ArrayList<>()).add(t.intervalo());
        return porDia;
    }

    private void validar(TrabalhoSuplementar t) {
        FuncionarioId funcionarioId = t.getFuncionarioId();
        var f = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Funcionário não encontrado: " + funcionarioId.getStringValor()));
        if (!Boolean.TRUE.equals(f.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");
        boolean isento = contratoRepository.findCurrentByFuncionarioId(funcionarioId)
                .map(c -> RegimeTrabalho.ISENCAO_HORARIO.getCode().equals(c.getRegimeTrabalho()))
                .orElse(false);
        if (isento)
            throw invalido("Quem tem isenção de horário não faz trabalho suplementar: a isenção já é um suplemento "
                    + "permanente (Lei n.º 20/X/2023, art. 155.º n.º 2 b)).");

        Horario horario = horarioColaboradorService.vigente(funcionarioId, t.getData()).horario();
        if (horario == null)
            throw invalido("Sem horário em " + t.getData() + " não se distingue o tempo normal do suplementar: "
                    + "marcar o horário base da instituição ou atribuir um.");
        Set<LocalDate> feriados = calendarioFeriadosService.feriadosDoColaborador(funcionarioId, t.getData(), t.getData());
        if (tipoDia(t.getData(), horario, feriados) == TipoDiaSuplementar.DIA_UTIL
                && t.tocaEm(blocosDoDia(horario, t.getData())))
            throw invalido("Num dia útil o trabalho suplementar fica fora do horário (" + horario.getNome()
                    + "): o intervalo " + t.getHoraInicio() + "–" + t.getHoraFim() + " toca num bloco do horário.");

        boolean sobrepoe = trabalhoRepository.findByFuncionarioEntre(funcionarioId, t.getData(), t.getData()).stream()
                .anyMatch(o -> o.emVigor() && !o.getId().equals(t.getId()) && o.sobrepoe(t));
        if (sobrepoe)
            throw IgrpResponseStatusException.conflict("Já há trabalho suplementar pedido ou autorizado que se sobrepõe a "
                    + t.getData() + " " + t.getHoraInicio() + "–" + t.getHoraFim() + ".");
    }

    /** FERIADO pelo calendário do colaborador; DESCANSO num dia sem blocos no horário; senão DIA_UTIL. */
    static TipoDiaSuplementar tipoDia(LocalDate data, Horario horario, Set<LocalDate> feriados) {
        if (feriados.contains(data)) return TipoDiaSuplementar.FERIADO;
        if (horario == null) return null;
        return horario.minutosNoDia(data.getDayOfWeek()) == 0 ? TipoDiaSuplementar.DESCANSO : TipoDiaSuplementar.DIA_UTIL;
    }

    private static List<BlocoHorario> blocosDoDia(Horario horario, LocalDate data) {
        if (horario == null) return List.of();
        return horario.getBlocos().stream().filter(b -> b.dia() == data.getDayOfWeek()).toList();
    }

    private void exigirChefiaDe(FuncionarioId chefe, FuncionarioId funcionarioId) {
        if (chefe.equals(funcionarioId))
            throw invalido("Ninguém autoriza o seu próprio trabalho suplementar.");
        if (!chefiaService.eChefeDirecto(chefe, funcionarioId))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN,
                    "Só a chefia directa (ou o RH) autoriza trabalho suplementar deste colaborador.");
    }

    private TrabalhoSuplementar encontrar(FuncionarioId funcionarioId, TrabalhoSuplementarId id) {
        return trabalhoRepository.findById(id)
                .filter(t -> funcionarioId == null || t.getFuncionarioId().equals(funcionarioId))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Trabalho suplementar não encontrado: " + id.getStringValor()));
    }

    LocalDateTime agora() { return LocalDateTime.now(); }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
