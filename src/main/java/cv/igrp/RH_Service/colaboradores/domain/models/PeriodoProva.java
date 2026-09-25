package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PeriodoProvaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProvimentoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>O período de prova do início do vínculo</b> (BR-PRV-07..14):
 * <ul>
 *   <li><b>Estágio probatório</b> (Lei n.º 20/X/2023, arts. 57.º e 72.º): <b>1 ano</b>, com tutor, que remete o
 *       relatório final da avaliação; pode cessar antes, por relatório fundamentado (art. 72.º n.º 6); o tempo
 *       com sucesso conta na carreira e categoria (n.º 5).</li>
 *   <li><b>Período experimental</b> (arts. 79.º–81.º) nos contratos a termo: <b>60 dias</b> no termo certo ≥ 6 meses
 *       (ou incerto que se preveja superior), <b>30 dias</b> nos restantes; a entidade pode fazer cessar o contrato
 *       por acto fundamentado, o agente pode denunciá-lo sem aviso prévio — sem indemnização.</li>
 * </ul>
 */
@Getter
public class PeriodoProva {

    public enum Tipo { ESTAGIO_PROBATORIO, PERIODO_EXPERIMENTAL }

    public enum Estado { EM_CURSO, CONCLUIDO_COM_SUCESSO, CONCLUIDO_SEM_SUCESSO, CESSADO_ANTECIPADAMENTE, DENUNCIADO }

    public enum Avaliacao { POSITIVA, NEGATIVA }

    /** Art. 72.º n.º 4. */
    public static final int MESES_ESTAGIO = 12;
    /** Art. 80.º n.º 1 a). */
    public static final int DIAS_EXPERIMENTAL_LONGO = 60;
    /** Art. 80.º n.º 1 b). */
    public static final int DIAS_EXPERIMENTAL_CURTO = 30;
    /** O limiar dos seis meses do art. 80.º. */
    public static final int MESES_LIMIAR_CONTRATO = 6;

    private PeriodoProvaId id;
    private ProvimentoId provimentoId;
    private FuncionarioId funcionarioId;
    private Tipo tipo;
    private LocalDate inicio;
    private LocalDate fimPrevisto;
    private FuncionarioId tutorId;
    private Estado estado;
    private LocalDate dataRelatorio;
    private Avaliacao avaliacao;
    private String fundamentacao;
    private LocalDate dataFim;

    private PeriodoProva() {}

    public static PeriodoProva estagio(ProvimentoId provimentoId, FuncionarioId funcionarioId, LocalDate inicio, FuncionarioId tutorId) {
        if (tutorId == null) throw invalido("O estágio probatório tem tutor: indique quem acompanha e avalia o estagiário.");
        if (tutorId.equals(funcionarioId)) throw invalido("O tutor não pode ser o próprio estagiário.");
        var p = novo(provimentoId, funcionarioId, Tipo.ESTAGIO_PROBATORIO, inicio);
        p.tutorId = tutorId;
        p.fimPrevisto = inicio.plusMonths(MESES_ESTAGIO).minusDays(1);
        return p;
    }

    /**
     * Período experimental: 60 dias quando o contrato dura (ou se prevê que dure) 6 meses ou mais, 30 quando menos.
     * {@code fimContrato} é o termo certo; no incerto, {@code mesesPrevistos} é a duração prevista.
     */
    public static PeriodoProva experimental(ProvimentoId provimentoId, FuncionarioId funcionarioId, LocalDate inicio,
                                            LocalDate fimContrato, Integer mesesPrevistos) {
        boolean longo;
        if (fimContrato != null) longo = !fimContrato.isBefore(inicio.plusMonths(MESES_LIMIAR_CONTRATO).minusDays(1));
        else if (mesesPrevistos != null) longo = mesesPrevistos >= MESES_LIMIAR_CONTRATO;
        else throw invalido("Para o período experimental é preciso saber quanto dura o contrato: registe a data do termo, "
                    + "ou indique a duração prevista em meses (termo incerto).");
        var p = novo(provimentoId, funcionarioId, Tipo.PERIODO_EXPERIMENTAL, inicio);
        p.fimPrevisto = inicio.plusDays((longo ? DIAS_EXPERIMENTAL_LONGO : DIAS_EXPERIMENTAL_CURTO) - 1);
        return p;
    }

    private static PeriodoProva novo(ProvimentoId provimentoId, FuncionarioId funcionarioId, Tipo tipo, LocalDate inicio) {
        var p = new PeriodoProva();
        p.id = PeriodoProvaId.gerarNovo();
        p.provimentoId = Objects.requireNonNull(provimentoId);
        p.funcionarioId = Objects.requireNonNull(funcionarioId);
        p.tipo = tipo;
        p.inicio = Objects.requireNonNull(inicio);
        p.estado = Estado.EM_CURSO;
        return p;
    }

    public static PeriodoProva reconstruir(PeriodoProvaId id, ProvimentoId provimentoId, FuncionarioId funcionarioId, Tipo tipo,
                                           LocalDate inicio, LocalDate fimPrevisto, FuncionarioId tutorId, Estado estado,
                                           LocalDate dataRelatorio, Avaliacao avaliacao, String fundamentacao, LocalDate dataFim) {
        var p = new PeriodoProva();
        p.id = id;
        p.provimentoId = provimentoId;
        p.funcionarioId = funcionarioId;
        p.tipo = tipo;
        p.inicio = inicio;
        p.fimPrevisto = fimPrevisto;
        p.tutorId = tutorId;
        p.estado = estado;
        p.dataRelatorio = dataRelatorio;
        p.avaliacao = avaliacao;
        p.fundamentacao = fundamentacao;
        p.dataFim = dataFim;
        return p;
    }

    /**
     * O tutor remete o relatório final (art. 72.º n.º 4); quem decide é a entidade competente ({@link #concluir}).
     * Pode registar-se antes do fim previsto (é o que o aviso dos 30 dias pede) e corrigir-se até à decisão.
     */
    public void registarRelatorio(FuncionarioId autor, Avaliacao avaliacao, String fundamentacao, LocalDate data) {
        exigirEmCurso();
        if (tipo != Tipo.ESTAGIO_PROBATORIO) throw invalido("O relatório do tutor é do estágio probatório.");
        if (autor != null && !autor.equals(tutorId))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Só o tutor do estágio regista o relatório.");
        if (avaliacao == null) throw invalido("Indique a avaliação: positiva ou negativa.");
        if (texto(fundamentacao) == null) throw invalido("O relatório final do estágio é fundamentado: descreva a avaliação.");
        this.avaliacao = avaliacao;
        this.fundamentacao = texto(fundamentacao);
        this.dataRelatorio = data != null ? data : LocalDate.now();
    }

    /**
     * O fim do período com o relatório (estágio: o do tutor, art. 72.º n.º 4) ou sem ele (o período experimental
     * que corre sem incidentes). Não antes do fim previsto — antes disso é cessação antecipada.
     */
    public void concluir(Avaliacao avaliacao, String fundamentacao, LocalDate data) {
        exigirEmCurso();
        // Sem avaliação na decisão, vale a do relatório do tutor.
        if (avaliacao == null) avaliacao = this.avaliacao;
        if (texto(fundamentacao) == null) fundamentacao = this.fundamentacao;
        if (avaliacao == null) throw invalido("Indique a avaliação: positiva ou negativa.");
        if (data == null) throw invalido("Indique a data do relatório.");
        if (data.isBefore(fimPrevisto))
            throw invalido("O período só termina a " + Datas.pt(fimPrevisto) + ". Antes disso, use a cessação antecipada.");
        if (tipo == Tipo.ESTAGIO_PROBATORIO && texto(fundamentacao) == null)
            throw invalido("O relatório final do estágio é fundamentado: descreva a avaliação.");
        this.avaliacao = avaliacao;
        this.fundamentacao = texto(fundamentacao);
        this.dataRelatorio = data;
        this.dataFim = data;
        this.estado = avaliacao == Avaliacao.POSITIVA ? Estado.CONCLUIDO_COM_SUCESSO : Estado.CONCLUIDO_SEM_SUCESSO;
    }

    /** Art. 72.º n.º 6 e art. 81.º n.º 1: por relatório (acto) fundamentado, quando manifestamente não tem as competências. */
    public void cessarAntecipadamente(String fundamentacao, LocalDate data) {
        exigirEmCurso();
        if (texto(fundamentacao) == null)
            throw invalido("A cessação antecipada é fundamentada: diga porque o colaborador não revela as competências exigidas.");
        if (data == null || data.isBefore(inicio)) throw invalido("Indique a data da cessação (não antes do início).");
        this.avaliacao = Avaliacao.NEGATIVA;
        this.fundamentacao = texto(fundamentacao);
        this.dataRelatorio = data;
        this.dataFim = data;
        this.estado = Estado.CESSADO_ANTECIPADAMENTE;
    }

    /** Art. 81.º n.º 2: o agente denuncia o contrato durante o período experimental, sem aviso prévio nem justa causa. */
    public void denunciar(LocalDate data) {
        exigirEmCurso();
        if (tipo != Tipo.PERIODO_EXPERIMENTAL)
            throw invalido("A denúncia sem aviso prévio é do período experimental; no estágio, o estagiário pede a exoneração.");
        if (data == null || data.isBefore(inicio)) throw invalido("Indique a data da denúncia (não antes do início).");
        this.dataFim = data;
        this.estado = Estado.DENUNCIADO;
    }

    public boolean emCurso() {
        return estado == Estado.EM_CURSO;
    }

    public boolean terminouSemSucesso() {
        return estado == Estado.CONCLUIDO_SEM_SUCESSO || estado == Estado.CESSADO_ANTECIPADAMENTE || estado == Estado.DENUNCIADO;
    }

    private void exigirEmCurso() {
        if (!emCurso()) throw IgrpResponseStatusException.conflict("Este período de prova já terminou.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
