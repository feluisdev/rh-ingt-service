package cv.igrp.RH_Service.recrutamento.domain.models;

import cv.igrp.RH_Service.recrutamento.domain.valueobject.CandidaturaId;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * <b>Uma candidatura</b> (BR-CNC-11..20): registada pelo RH (decisão do cliente: sem portal), com os dados do
 * candidato — que pode ser um funcionário da casa ({@link #funcionarioId}). Admitida ou excluída (a exclusão abre a
 * audiência dos interessados), avaliada por método (0–20; eliminatório abaixo da nota mínima), classificada pela média
 * ponderada, ordenada, e no fim provida num Lugar, em reserva, ou desistente.
 */
@Getter
public class Candidatura {

    public enum Estado { APRESENTADA, EM_AUDIENCIA, ADMITIDA, EXCLUIDA, REPROVADA, APROVADA, PROVIDA, DESISTIU }

    /** Audiência dos interessados antes da exclusão definitiva [ind.]: 10 dias. */
    public static final int DIAS_AUDIENCIA = 10;

    private CandidaturaId id;
    private ConcursoId concursoId;
    private String nome;
    private String documento;
    private String nif;
    private String email;
    private String telefone;
    private String habilitacao;
    private boolean deficiencia;
    /** O candidato é funcionário da casa. */
    private UUID funcionarioId;
    /** Vinculado à Administração Pública (para os concursos internos, se não for da casa). */
    private boolean vinculadoAdministracao;
    private LocalDate dataApresentacao;
    private Estado estado;
    private String motivoExclusao;
    private LocalDate audienciaAte;
    private String respostaAudiencia;
    private Map<MetodoSelecao, BigDecimal> notas = new EnumMap<>(MetodoSelecao.class);
    private BigDecimal classificacaoFinal;
    private Integer posicao;
    private UUID lugarProvidoId;
    private LocalDate dataDesistencia;

    private Candidatura() {}

    public static Candidatura apresentar(Concurso concurso, String nome, String documento, String nif, String email, String telefone,
                                         String habilitacao, boolean deficiencia, UUID funcionarioId, boolean vinculadoAdministracao,
                                         LocalDate data) {
        Objects.requireNonNull(concurso);
        if (nome == null || nome.isBlank()) throw invalido("Indique o nome do candidato.");
        if ((documento == null || documento.isBlank()) && funcionarioId == null)
            throw invalido("Indique o documento de identificação do candidato.");
        if (!concurso.aceitaCandidatura(data))
            throw invalido(concurso.getEstado() != Concurso.Estado.ABERTO ? "O concurso não está aberto a candidaturas."
                    : "O prazo de candidatura vai de " + Datas.pt(concurso.getCandidaturasDe()) + " a " + Datas.pt(concurso.getCandidaturasAte()) + ".");
        var c = new Candidatura();
        c.id = CandidaturaId.gerarNovo();
        c.concursoId = concurso.getId();
        c.nome = nome.trim();
        c.documento = documento == null || documento.isBlank() ? null : documento.trim().toUpperCase();
        c.nif = texto(nif);
        c.email = texto(email);
        c.telefone = texto(telefone);
        c.habilitacao = texto(habilitacao);
        c.deficiencia = deficiencia;
        c.funcionarioId = funcionarioId;
        c.vinculadoAdministracao = vinculadoAdministracao || funcionarioId != null;
        c.dataApresentacao = data;
        c.estado = Estado.APRESENTADA;
        return c;
    }

    public static Candidatura reconstruir(CandidaturaId id, ConcursoId concursoId, String nome, String documento, String nif, String email,
                                          String telefone, String habilitacao, boolean deficiencia, UUID funcionarioId,
                                          boolean vinculadoAdministracao, LocalDate dataApresentacao, Estado estado, String motivoExclusao,
                                          LocalDate audienciaAte, String respostaAudiencia, Map<MetodoSelecao, BigDecimal> notas,
                                          BigDecimal classificacaoFinal, Integer posicao, UUID lugarProvidoId, LocalDate dataDesistencia) {
        var c = new Candidatura();
        c.id = id;
        c.concursoId = concursoId;
        c.nome = nome;
        c.documento = documento;
        c.nif = nif;
        c.email = email;
        c.telefone = telefone;
        c.habilitacao = habilitacao;
        c.deficiencia = deficiencia;
        c.funcionarioId = funcionarioId;
        c.vinculadoAdministracao = vinculadoAdministracao;
        c.dataApresentacao = dataApresentacao;
        c.estado = estado;
        c.motivoExclusao = motivoExclusao;
        c.audienciaAte = audienciaAte;
        c.respostaAudiencia = respostaAudiencia;
        c.notas = notas.isEmpty() ? new EnumMap<>(MetodoSelecao.class) : new EnumMap<>(notas);
        c.classificacaoFinal = classificacaoFinal;
        c.posicao = posicao;
        c.lugarProvidoId = lugarProvidoId;
        c.dataDesistencia = dataDesistencia;
        return c;
    }

    // ---------------------------------------------------------------- admissão

    public void admitir() {
        if (estado != Estado.APRESENTADA && estado != Estado.EM_AUDIENCIA)
            throw IgrpResponseStatusException.conflict("Esta candidatura já foi apreciada.");
        this.estado = Estado.ADMITIDA;
        this.motivoExclusao = null;
    }

    /** A exclusão não é logo definitiva: abre a audiência dos interessados. */
    public void proporExclusao(String motivo, LocalDate hoje) {
        if (estado != Estado.APRESENTADA) throw IgrpResponseStatusException.conflict("Esta candidatura já foi apreciada.");
        if (motivo == null || motivo.isBlank()) throw invalido("Indique o motivo da exclusão.");
        this.motivoExclusao = motivo.trim();
        this.audienciaAte = hoje.plusDays(DIAS_AUDIENCIA);
        this.estado = Estado.EM_AUDIENCIA;
    }

    /** Depois da audiência: excluída de vez, ou admitida (o júri aceitou a resposta). */
    public void decidirAudiencia(boolean excluir, String resposta) {
        if (estado != Estado.EM_AUDIENCIA) throw IgrpResponseStatusException.conflict("Esta candidatura não está em audiência.");
        this.respostaAudiencia = texto(resposta);
        if (excluir) this.estado = Estado.EXCLUIDA;
        else admitir();
    }

    // ---------------------------------------------------------------- avaliação

    /**
     * Uma nota de 0 a 20 num dos métodos do concurso. Num eliminatório, abaixo da mínima, reprova; corrigida, o estado
     * recalcula-se com todas as notas.
     */
    public void registarNota(java.util.List<Concurso.Metodo> metodos, MetodoSelecao metodo, BigDecimal nota) {
        if (estado != Estado.ADMITIDA && estado != Estado.REPROVADA)
            throw IgrpResponseStatusException.conflict("Só se avaliam candidaturas admitidas.");
        if (metodos.stream().noneMatch(m -> m.metodo() == metodo)) throw invalido("Esse método não faz parte deste concurso.");
        if (nota == null || nota.signum() < 0 || nota.compareTo(Concurso.NOTA_MAXIMA) > 0)
            throw invalido("A nota fica entre 0 e 20 valores.");
        notas.put(metodo, nota);
        boolean eliminado = metodos.stream().anyMatch(m -> m.eliminatorio() && m.notaMinima() != null
                && notas.containsKey(m.metodo()) && notas.get(m.metodo()).compareTo(m.notaMinima()) < 0);
        this.estado = eliminado ? Estado.REPROVADA : Estado.ADMITIDA;
    }

    public boolean temTodasAsNotas(java.util.List<Concurso.Metodo> metodos) {
        return metodos.stream().allMatch(m -> notas.containsKey(m.metodo()));
    }

    /** A média ponderada (duas casas), ou reprovado se algum eliminatório ficou abaixo do mínimo. */
    public void classificar(java.util.List<Concurso.Metodo> metodos) {
        if (estado == Estado.REPROVADA || estado != Estado.ADMITIDA) return;
        BigDecimal total = BigDecimal.ZERO;
        for (Concurso.Metodo m : metodos) {
            BigDecimal n = notas.get(m.metodo());
            if (n == null) return;
            if (m.eliminatorio() && m.notaMinima() != null && n.compareTo(m.notaMinima()) < 0) {
                this.estado = Estado.REPROVADA;
                return;
            }
            total = total.add(n.multiply(BigDecimal.valueOf(m.ponderacao())));
        }
        this.classificacaoFinal = total.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        this.estado = Estado.APROVADA;
    }

    public void posicionar(int posicao) {
        this.posicao = posicao;
    }

    /** Provida num Lugar do concurso, depois da homologação. */
    public void prover(UUID lugarId) {
        if (estado != Estado.APROVADA) throw IgrpResponseStatusException.conflict("Só se provêem candidatos aprovados.");
        this.lugarProvidoId = Objects.requireNonNull(lugarId);
        this.estado = Estado.PROVIDA;
    }

    public void desistir(LocalDate data) {
        if (estado == Estado.DESISTIU || estado == Estado.EXCLUIDA) throw IgrpResponseStatusException.conflict("Esta candidatura já terminou.");
        this.lugarProvidoId = null;
        this.dataDesistencia = data;
        this.estado = Estado.DESISTIU;
    }

    public boolean aprovada() {
        return estado == Estado.APROVADA || estado == Estado.PROVIDA;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
