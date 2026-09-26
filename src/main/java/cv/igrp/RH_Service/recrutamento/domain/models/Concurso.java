package cv.igrp.RH_Service.recrutamento.domain.models;

import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * <b>Um procedimento concursal</b> (Lei n.º 20/X/2023, arts. 123.º–129.º; BR-CNC-01..10): o concurso é obrigatório
 * para o ingresso e o acesso (art. 123.º). Define os Lugares a prover (vagos, da categoria), os requisitos, os métodos
 * com a sua ponderação, o júri e o prazo de candidatura, e segue o ciclo até à lista homologada.
 *
 * <p>RASCUNHO → ABERTO → CANDIDATURAS_ENCERRADAS → EM_AVALIACAO → LISTA_PROVISORIA → HOMOLOGADO → CONCLUIDO;
 * ANULADO. Enquanto em RASCUNHO, tudo se edita; aberto, só avança.
 */
@Getter
public class Concurso {

    public enum Finalidade { INGRESSO, ACESSO }
    /** Art. 127.º n.º 1. */
    public enum Tipo { COMUM, ESPECIAL }
    /** Art. 127.º n.os 2 e 3. */
    public enum Modalidade { EXTERNO, INTERNO, INTERNO_RESTRITO }
    public enum Estado { RASCUNHO, ABERTO, CANDIDATURAS_ENCERRADAS, EM_AVALIACAO, LISTA_PROVISORIA, HOMOLOGADO, CONCLUIDO, ANULADO }
    public enum PapelJuri { PRESIDENTE, VOGAL, SUPLENTE }

    /** Um método com a sua ponderação (em %, somam 100), se é eliminatório e a nota mínima (0–20). */
    public record Metodo(MetodoSelecao metodo, int ponderacao, boolean eliminatorio, BigDecimal notaMinima) {}

    /** Um membro do júri: um funcionário da casa ou uma pessoa de fora (só o nome). */
    public record MembroJuri(PapelJuri papel, String nome, UUID funcionarioId) {}

    /** Escala da classificação [ind.]: 0 a 20 valores. */
    public static final BigDecimal NOTA_MAXIMA = BigDecimal.valueOf(20);
    /** Validade da lista homologada como reserva de recrutamento [ind.]: 18 meses. */
    public static final int MESES_RESERVA = 18;

    private ConcursoId id;
    private String referencia;
    private Finalidade finalidade;
    private Tipo tipo;
    private Modalidade modalidade;
    /** A forma de vínculo a constituir (o nome de ModalidadeProvimento: NOMEACAO_PROVISORIA, CONTRATO_TERMO_CERTO...). */
    private String vinculo;
    private UUID categoriaId;
    private List<UUID> lugares = new ArrayList<>();
    private String requisitos;
    private String habilitacaoMinima;
    /** Art. 127.º n.º 4: nos externos, o número de lugares reservado a pessoas com deficiência. */
    private Integer quotaDeficiencia;
    private List<Metodo> metodos = new ArrayList<>();
    /** Art. 128.º n.º 5: a dispensa de métodos obrigatórios, por despacho. */
    private String dispensaMetodosDespacho;
    private List<MembroJuri> juri = new ArrayList<>();
    private LocalDate dataAviso;
    private LocalDate candidaturasDe;
    private LocalDate candidaturasAte;
    private Estado estado;
    private String homologacaoDespacho;
    private LocalDate homologacaoData;
    private LocalDate reservaAte;
    private String motivoAnulacao;

    private Concurso() {}

    public static Concurso criar(String referencia, Finalidade finalidade, Tipo tipo, Modalidade modalidade, String vinculo,
                                 UUID categoriaId) {
        var c = new Concurso();
        c.id = ConcursoId.gerarNovo();
        c.estado = Estado.RASCUNHO;
        c.definir(referencia, finalidade, tipo, modalidade, vinculo, categoriaId);
        return c;
    }

    public static Concurso reconstruir(ConcursoId id, String referencia, Finalidade finalidade, Tipo tipo, Modalidade modalidade,
                                       String vinculo, UUID categoriaId, List<UUID> lugares, String requisitos,
                                       String habilitacaoMinima, Integer quotaDeficiencia, List<Metodo> metodos,
                                       String dispensaMetodosDespacho, List<MembroJuri> juri, LocalDate dataAviso,
                                       LocalDate candidaturasDe, LocalDate candidaturasAte, Estado estado,
                                       String homologacaoDespacho, LocalDate homologacaoData, LocalDate reservaAte,
                                       String motivoAnulacao) {
        var c = new Concurso();
        c.id = id;
        c.referencia = referencia;
        c.finalidade = finalidade;
        c.tipo = tipo;
        c.modalidade = modalidade;
        c.vinculo = vinculo;
        c.categoriaId = categoriaId;
        c.lugares = new ArrayList<>(lugares);
        c.requisitos = requisitos;
        c.habilitacaoMinima = habilitacaoMinima;
        c.quotaDeficiencia = quotaDeficiencia;
        c.metodos = new ArrayList<>(metodos);
        c.dispensaMetodosDespacho = dispensaMetodosDespacho;
        c.juri = new ArrayList<>(juri);
        c.dataAviso = dataAviso;
        c.candidaturasDe = candidaturasDe;
        c.candidaturasAte = candidaturasAte;
        c.estado = estado;
        c.homologacaoDespacho = homologacaoDespacho;
        c.homologacaoData = homologacaoData;
        c.reservaAte = reservaAte;
        c.motivoAnulacao = motivoAnulacao;
        return c;
    }

    // ---------------------------------------------------------------- rascunho

    public void definir(String referencia, Finalidade finalidade, Tipo tipo, Modalidade modalidade, String vinculo, UUID categoriaId) {
        exigirRascunho();
        if (referencia == null || referencia.isBlank()) throw invalido("Indique a referência do concurso.");
        if (finalidade == null) throw invalido("Diga se o concurso é de ingresso ou de acesso.");
        if (modalidade == null) throw invalido("Indique a modalidade: externo, interno ou interno restrito.");
        if (categoriaId == null) throw invalido("Indique a categoria dos Lugares a prover.");
        this.referencia = referencia.trim();
        this.finalidade = finalidade;
        this.tipo = tipo != null ? tipo : Tipo.COMUM;
        this.modalidade = modalidade;
        this.vinculo = vinculo == null || vinculo.isBlank() ? null : vinculo.trim().toUpperCase();
        this.categoriaId = categoriaId;
    }

    public void definirRequisitos(String requisitos, String habilitacaoMinima, Integer quotaDeficiencia) {
        exigirRascunho();
        if (quotaDeficiencia != null && quotaDeficiencia < 0) throw invalido("A quota não pode ser negativa.");
        this.requisitos = texto(requisitos);
        this.habilitacaoMinima = texto(habilitacaoMinima);
        this.quotaDeficiencia = quotaDeficiencia;
    }

    public void definirLugares(List<UUID> lugares) {
        exigirRascunho();
        if (lugares == null || lugares.isEmpty()) throw invalido("Indique pelo menos um Lugar a prover.");
        if (lugares.stream().distinct().count() != lugares.size()) throw invalido("Há Lugares repetidos.");
        this.lugares = new ArrayList<>(lugares);
    }

    public void definirMetodos(List<Metodo> metodos, String dispensaDespacho) {
        exigirRascunho();
        if (metodos == null || metodos.isEmpty()) throw invalido("Indique os métodos de selecção.");
        if (metodos.stream().map(Metodo::metodo).distinct().count() != metodos.size()) throw invalido("Há métodos repetidos.");
        for (Metodo m : metodos) {
            if (m.metodo() == null) throw invalido("Há um método sem tipo.");
            if (m.ponderacao() <= 0 || m.ponderacao() > 100) throw invalido("Cada método pesa entre 1% e 100%.");
            if (m.notaMinima() != null && (m.notaMinima().signum() < 0 || m.notaMinima().compareTo(NOTA_MAXIMA) > 0))
                throw invalido("A nota mínima fica entre 0 e 20 valores.");
        }
        if (metodos.stream().mapToInt(Metodo::ponderacao).sum() != 100) throw invalido("As ponderações dos métodos têm de somar 100%.");
        this.metodos = new ArrayList<>(metodos);
        this.dispensaMetodosDespacho = texto(dispensaDespacho);
    }

    public void definirJuri(List<MembroJuri> juri) {
        exigirRascunho();
        if (juri == null) juri = List.of();
        long presidentes = juri.stream().filter(m -> m.papel() == PapelJuri.PRESIDENTE).count();
        long vogais = juri.stream().filter(m -> m.papel() == PapelJuri.VOGAL).count();
        long suplentes = juri.stream().filter(m -> m.papel() == PapelJuri.SUPLENTE).count();
        if (presidentes != 1 || vogais < 2 || suplentes < 2)
            throw invalido("O júri tem um presidente, pelo menos dois vogais efectivos e dois suplentes.");
        if (juri.stream().anyMatch(m -> m.nome() == null || m.nome().isBlank())) throw invalido("Cada membro do júri tem nome.");
        this.juri = new ArrayList<>(juri);
    }

    public void definirPrazo(LocalDate dataAviso, LocalDate de, LocalDate ate) {
        exigirRascunho();
        if (de == null || ate == null) throw invalido("Indique o prazo de candidatura (de e até).");
        if (ate.isBefore(de)) throw invalido("O fim do prazo de candidatura é antes do início.");
        if (dataAviso != null && de.isBefore(dataAviso)) throw invalido("O prazo de candidatura não começa antes do aviso.");
        this.dataAviso = dataAviso;
        this.candidaturasDe = de;
        this.candidaturasAte = ate;
    }

    /**
     * Abrir é publicar o aviso: tudo definido, e os métodos obrigatórios presentes — salvo o contrato a termo, que se
     * pode limitar à triagem e à entrevista (art. 128.º n.º 3), ou a dispensa por despacho (n.º 5).
     */
    public void abrir(LocalDate hoje) {
        exigirRascunho();
        if (lugares.isEmpty()) throw invalido("Indique os Lugares a prover antes de abrir o concurso.");
        if (metodos.isEmpty()) throw invalido("Defina os métodos de selecção antes de abrir o concurso.");
        if (juri.isEmpty()) throw invalido("Nomeie o júri antes de abrir o concurso.");
        if (candidaturasDe == null) throw invalido("Defina o prazo de candidatura antes de abrir o concurso.");
        if (modalidade == Modalidade.EXTERNO && quotaDeficiencia == null)
            throw invalido("Nos concursos externos é obrigatória a quota de lugares para pessoas com deficiência (pode ser 0 quando o número de lugares não a comporta).");
        if (quotaDeficiencia != null && quotaDeficiencia > lugares.size())
            throw invalido("A quota não pode passar do número de Lugares a prover.");
        Set<MetodoSelecao> usados = metodos.stream().map(Metodo::metodo).collect(Collectors.toCollection(() -> EnumSet.noneOf(MetodoSelecao.class)));
        Set<MetodoSelecao> exigidos = aTermo() ? EnumSet.of(MetodoSelecao.TRIAGEM_CURRICULAR, MetodoSelecao.ENTREVISTA)
                : EnumSet.of(MetodoSelecao.TRIAGEM_CURRICULAR, MetodoSelecao.PROVA_CONHECIMENTOS, MetodoSelecao.AVALIACAO_COMPETENCIAS,
                MetodoSelecao.ENTREVISTA);
        exigidos.removeAll(usados);
        if (!exigidos.isEmpty() && dispensaMetodosDespacho == null)
            throw invalido("Faltam métodos obrigatórios (" + exigidos.stream().map(Concurso::nomeMetodo).collect(Collectors.joining(", "))
                    + "). Inclua-os, ou registe o despacho que os dispensa.");
        if (dataAviso == null) dataAviso = hoje;
        this.estado = Estado.ABERTO;
    }

    public boolean aTermo() {
        return vinculo != null && vinculo.startsWith("CONTRATO_TERMO");
    }

    public boolean aceitaCandidatura(LocalDate em) {
        return estado == Estado.ABERTO && !em.isBefore(candidaturasDe) && !em.isAfter(candidaturasAte);
    }

    public void encerrarCandidaturas(LocalDate hoje) {
        exigir(Estado.ABERTO, "encerrar as candidaturas");
        if (!hoje.isAfter(candidaturasAte)) throw invalido("O prazo de candidatura só termina a " + Datas.pt(candidaturasAte) + ".");
        this.estado = Estado.CANDIDATURAS_ENCERRADAS;
    }

    /** A lista de admitidos e excluídos está assente (as audiências terminaram): começa a avaliação. */
    public void iniciarAvaliacao(boolean audienciasPendentes) {
        exigir(Estado.CANDIDATURAS_ENCERRADAS, "iniciar a avaliação");
        if (audienciasPendentes) throw IgrpResponseStatusException.conflict("Há exclusões em audiência dos interessados: decida-as primeiro.");
        this.estado = Estado.EM_AVALIACAO;
    }

    public void listaProvisoria(boolean notasEmFalta) {
        exigir(Estado.EM_AVALIACAO, "publicar a lista provisória");
        if (notasEmFalta) throw IgrpResponseStatusException.conflict("Há candidatos admitidos sem todas as notas: registe-as primeiro.");
        this.estado = Estado.LISTA_PROVISORIA;
    }

    public void homologar(String despacho, LocalDate data) {
        exigir(Estado.LISTA_PROVISORIA, "homologar");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho de homologação da lista.");
        if (data == null) throw invalido("Indique a data da homologação.");
        this.homologacaoDespacho = despacho.trim();
        this.homologacaoData = data;
        this.reservaAte = data.plusMonths(MESES_RESERVA);
        this.estado = Estado.HOMOLOGADO;
    }

    public void concluir() {
        exigir(Estado.HOMOLOGADO, "concluir");
        this.estado = Estado.CONCLUIDO;
    }

    public void anular(String motivo) {
        if (estado == Estado.CONCLUIDO || estado == Estado.ANULADO) throw IgrpResponseStatusException.conflict("Este concurso já terminou.");
        if (motivo == null || motivo.isBlank()) throw invalido("Anular o concurso exige o motivo.");
        this.motivoAnulacao = motivo.trim();
        this.estado = Estado.ANULADO;
    }

    public boolean reservaValida(LocalDate em) {
        return estado == Estado.HOMOLOGADO && reservaAte != null && !em.isAfter(reservaAte);
    }

    static String nomeMetodo(MetodoSelecao m) {
        return switch (m) {
            case TRIAGEM_CURRICULAR -> "triagem curricular";
            case PROVA_CONHECIMENTOS -> "prova de conhecimentos";
            case AVALIACAO_COMPETENCIAS -> "avaliação de competências";
            case ENTREVISTA -> "entrevista de selecção";
            case CURSO_FORMACAO -> "curso de formação";
            case PROVAS_FISICAS -> "provas físicas";
        };
    }

    private void exigirRascunho() {
        if (estado != null && estado != Estado.RASCUNHO)
            throw IgrpResponseStatusException.conflict("O concurso já foi aberto: os seus termos não mudam.");
    }

    private void exigir(Estado esperado, String accao) {
        if (estado != esperado) throw IgrpResponseStatusException.conflict("Não é possível " + accao + " no estado em que o concurso está.");
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, Objects.requireNonNull(m));
    }
}
