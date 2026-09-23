package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Um horário do catálogo da instituição — assiduidade, primeiro passo (Lei n.º 20/X/2023,
 * arts. 164.º a 166.º).
 *
 * <p>O {@code nome} é livre, e é nele que a instituição diz a modalidade: o diploma de
 * desenvolvimento que as define (art. 165.º n.º 2) não está publicado. O código só conhece o
 * {@link ControloHorario}, que é a diferença que as regras usam.
 *
 * <p>Os limites legais — duração máxima diária e semanal, intervalo mínimo de descanso (art. 164.º
 * n.º 4) — também são do diploma de desenvolvimento e <b>não se validam</b>: não se inventam.
 * Validam-se só as invariantes que fazem de uma lista de blocos um horário.
 *
 * <p>O horário <b>base</b> é o da instituição: vale para quem não tem horário atribuído e trabalha
 * numa unidade sem horário, própria ou herdada da unidade-mãe. Há no máximo um.
 *
 * <p>As horas por dia e por semana <b>calculam-se</b> dos blocos (no fixo) ou da duração diária
 * (no flexível); não se guardam.
 */
@Getter
public class Horario {

    private HorarioId id;
    private String nome;
    private ControloHorario controlo;
    /** Só no flexível. */
    private PeriodoAfericao periodoAfericao;
    /** Só no flexível: o que se cumpre em cada dia que tenha blocos. No fixo, é a soma dos blocos. */
    private Integer duracaoDiariaMinutos;
    private List<BlocoHorario> blocos;
    private boolean base;
    private boolean active;

    private Horario() {}

    public static Horario criar(String nome, ControloHorario controlo, PeriodoAfericao periodoAfericao,
                                Integer duracaoDiariaMinutos, List<BlocoHorario> blocos) {
        var h = new Horario();
        h.id = HorarioId.gerarNovo();
        h.active = true;
        h.aplicar(nome, controlo, periodoAfericao, duracaoDiariaMinutos, blocos);
        return h;
    }

    public static Horario reconstruir(HorarioId id, String nome, ControloHorario controlo,
                                      PeriodoAfericao periodoAfericao, Integer duracaoDiariaMinutos,
                                      List<BlocoHorario> blocos, boolean base, boolean active) {
        var h = new Horario();
        h.id = id;
        h.nome = nome;
        h.controlo = controlo;
        h.periodoAfericao = periodoAfericao;
        h.duracaoDiariaMinutos = duracaoDiariaMinutos;
        h.blocos = List.copyOf(blocos);
        h.base = base;
        h.active = active;
        return h;
    }

    public void atualizar(String nome, ControloHorario controlo, PeriodoAfericao periodoAfericao,
                          Integer duracaoDiariaMinutos, List<BlocoHorario> blocos) {
        aplicar(nome, controlo, periodoAfericao, duracaoDiariaMinutos, blocos);
    }

    public void desativar() {
        if (!active) throw IgrpResponseStatusException.conflict("O horário já está inactivo.");
        // Sem base, quem não tem horário na pessoa nem na unidade ficava sem período normal.
        if (base) throw IgrpResponseStatusException.conflict(
                "O horário base não se desactiva: marque primeiro outro horário como base.");
        active = false;
    }

    /** Passa a ser o horário da instituição. Quem chama desmarca o anterior. */
    public void marcarComoBase() {
        if (!active) throw invalido("Um horário inactivo não pode ser o horário base.");
        base = true;
    }

    public void desmarcarBase() {
        base = false;
    }

    public void reativar() {
        if (active) throw IgrpResponseStatusException.conflict("O horário já está activo.");
        active = true;
    }

    /** Minutos do período normal num dia da semana: zero nos dias sem blocos. */
    public int minutosNoDia(DayOfWeek dia) {
        List<BlocoHorario> doDia = blocos.stream().filter(b -> b.dia() == dia).toList();
        if (doDia.isEmpty()) return 0;
        if (controlo == ControloHorario.FLEXIVEL) return duracaoDiariaMinutos;
        return doDia.stream().mapToInt(BlocoHorario::minutos).sum();
    }

    public int minutosSemanais() {
        int total = 0;
        for (DayOfWeek dia : DayOfWeek.values()) total += minutosNoDia(dia);
        return total;
    }

    private void aplicar(String nome, ControloHorario controlo, PeriodoAfericao periodoAfericao,
                         Integer duracaoDiariaMinutos, List<BlocoHorario> blocos) {
        if (nome == null || nome.isBlank())
            throw invalido("O nome do horário é obrigatório.");
        if (controlo == null)
            throw invalido("O controlo do horário é obrigatório: FIXO ou FLEXIVEL.");
        if (blocos == null || blocos.isEmpty())
            throw invalido("Um horário tem pelo menos um bloco.");

        List<BlocoHorario> normalizados = new ArrayList<>();
        for (BlocoHorario b : blocos) {
            if (b == null || b.dia() == null || b.inicio() == null || b.fim() == null)
                throw invalido("Cada bloco tem dia, início e fim.");
            // Um bloco que passa a meia-noite é trabalho por turnos, que é matéria do diploma de
            // desenvolvimento (art. 165.º n.º 4) e fica fora deste passo.
            if (!b.inicio().isBefore(b.fim()))
                throw invalido("Bloco de " + b.dia() + " com início (" + b.inicio()
                        + ") que não é antes do fim (" + b.fim() + ").");
            // No fixo não há margem: todos os blocos são período normal.
            normalizados.add(controlo == ControloHorario.FIXO
                    ? new BlocoHorario(b.dia(), b.inicio(), b.fim(), true) : b);
        }
        normalizados.sort(Comparator.comparing(BlocoHorario::dia).thenComparing(BlocoHorario::inicio));
        for (int i = 1; i < normalizados.size(); i++)
            if (normalizados.get(i - 1).sobrepoe(normalizados.get(i)))
                throw invalido("Blocos sobrepostos em " + normalizados.get(i).dia() + ".");

        if (controlo == ControloHorario.FIXO) {
            if (periodoAfericao != null || duracaoDiariaMinutos != null)
                throw invalido("O período de aferição e a duração diária são só do horário flexível.");
        } else {
            if (periodoAfericao == null)
                throw invalido("O horário flexível tem período de aferição: SEMANA ou MES (DL n.º 3/2010, art. 13.º n.º 2).");
            if (duracaoDiariaMinutos == null || duracaoDiariaMinutos <= 0)
                throw invalido("O horário flexível tem duração diária.");
            for (DayOfWeek dia : DayOfWeek.values()) {
                List<BlocoHorario> doDia = normalizados.stream().filter(b -> b.dia() == dia).toList();
                if (doDia.isEmpty()) continue;
                int total = doDia.stream().mapToInt(BlocoHorario::minutos).sum();
                int plataformas = doDia.stream().filter(BlocoHorario::obrigatorio).mapToInt(BlocoHorario::minutos).sum();
                if (duracaoDiariaMinutos > total)
                    throw invalido("A duração diária não cabe nos blocos de " + dia + ".");
                if (plataformas > duracaoDiariaMinutos)
                    throw invalido("As plataformas fixas de " + dia + " passam da duração diária.");
            }
        }

        this.nome = nome.trim();
        this.controlo = controlo;
        this.periodoAfericao = periodoAfericao;
        this.duracaoDiariaMinutos = duracaoDiariaMinutos;
        this.blocos = List.copyOf(normalizados);
    }

    private static IgrpResponseStatusException invalido(String mensagem) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, mensagem);
    }
}
