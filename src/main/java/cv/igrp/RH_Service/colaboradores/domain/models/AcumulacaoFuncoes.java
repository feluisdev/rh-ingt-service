package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcumulacaoFuncoesId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

/**
 * <b>Acumulação de funções</b> (Lei n.º 20/X/2023, arts. 20.º–24.º; BR-ACU-01..08). A regra é a exclusividade (art. 20.º);
 * a acumulação pede-se, autoriza-se por despacho (art. 23.º: não remunerada, o dirigente máximo; remunerada, os membros do
 * Governo) e termina — cessada, ou caducada no fim do período.
 *
 * <p>PEDIDA → AUTORIZADA / INDEFERIDA → CESSADA / CADUCADA.
 */
@Getter
public class AcumulacaoFuncoes {

    public enum Tipo { PUBLICA, PRIVADA }
    /** Art. 21.º n.º 2: os casos em que as funções públicas acumuladas podem ser remuneradas. */
    public enum CasoPublico { INERENCIA, REPRESENTACAO, COMISSAO_GRUPO_TRABALHO, ORGAO_COLEGIAL, DOCENCIA_INVESTIGACAO, CONFERENCIA_FORMACAO }
    /** Art. 23.º: quem autoriza. */
    public enum Autorizacao { DIRIGENTE_MAXIMO, MEMBROS_GOVERNO }
    public enum Estado { PEDIDA, AUTORIZADA, INDEFERIDA, CESSADA, CADUCADA }

    private AcumulacaoFuncoesId id;
    private FuncionarioId funcionarioId;
    private Tipo tipo;
    private CasoPublico casoPublico;
    private boolean remunerada;
    private String entidade;
    private String funcoes;
    private String horario;
    private Integer horasSemanais;
    private LocalDate inicio;
    private LocalDate fim;
    private boolean declaracaoSemConflito;
    private Estado estado;
    private String despacho;
    private LocalDate dataDespacho;
    private String motivo;
    private LocalDate dataFimEfectiva;

    private AcumulacaoFuncoes() {}

    /**
     * O pedido (BR-ACU-01..04). As públicas remuneradas só nos casos do art. 21.º n.º 2; a docência não passa de um terço do
     * horário semanal da função principal ({@code minutosSemanaisPrincipal}, se se sabe). As privadas exigem a declaração do
     * próprio de que não são concorrentes nem conflituantes, não se sobrepõem ao horário e não comprometem a isenção
     * (art. 22.º; art. 24.º).
     */
    public static AcumulacaoFuncoes pedir(FuncionarioId funcionarioId, Tipo tipo, CasoPublico caso, boolean remunerada, String entidade,
                                          String funcoes, String horario, Integer horasSemanais, LocalDate inicio, LocalDate fim,
                                          boolean declaracaoSemConflito, Integer minutosSemanaisPrincipal) {
        if (tipo == null) throw invalido("Diga se as funções a acumular são públicas ou privadas.");
        if (entidade == null || entidade.isBlank()) throw invalido("Indique a entidade onde exerceria as funções.");
        if (funcoes == null || funcoes.isBlank()) throw invalido("Descreva as funções a acumular.");
        if (inicio == null) throw invalido("Indique a data de início.");
        if (fim != null && fim.isBefore(inicio)) throw invalido("O fim não pode ser antes do início.");
        if (horasSemanais != null && horasSemanais < 1) throw invalido("As horas semanais, se as indica, são pelo menos uma.");
        if (tipo == Tipo.PUBLICA && remunerada && caso == null)
            throw invalido("Funções públicas remuneradas só se acumulam nos casos da lei: inerência, representação, comissões ou grupos de trabalho, "
                    + "órgãos colegiais, docência ou investigação, conferências e formação de curta duração (art. 21.º n.º 2).");
        if (caso == CasoPublico.DOCENCIA_INVESTIGACAO) {
            if (horasSemanais == null) throw invalido("Na docência ou investigação, indique as horas semanais.");
            if (minutosSemanaisPrincipal != null && minutosSemanaisPrincipal > 0 && horasSemanais * 60 * 3 > minutosSemanaisPrincipal)
                throw invalido("A docência ou investigação não pode passar de um terço do horário da função principal ("
                        + (minutosSemanaisPrincipal / 180) + " horas por semana) — art. 21.º n.º 2 d).");
        }
        if (tipo == Tipo.PRIVADA && !declaracaoSemConflito)
            throw invalido("Nas funções privadas, o próprio declara que não são concorrentes nem conflituantes, não se sobrepõem ao horário "
                    + "e não comprometem a isenção (arts. 22.º e 24.º).");
        var a = new AcumulacaoFuncoes();
        a.id = AcumulacaoFuncoesId.gerarNovo();
        a.funcionarioId = funcionarioId;
        a.tipo = tipo;
        a.casoPublico = tipo == Tipo.PUBLICA ? caso : null;
        a.remunerada = remunerada;
        a.entidade = entidade.trim();
        a.funcoes = funcoes.trim();
        a.horario = horario != null && !horario.isBlank() ? horario.trim() : null;
        a.horasSemanais = horasSemanais;
        a.inicio = inicio;
        a.fim = fim;
        a.declaracaoSemConflito = declaracaoSemConflito;
        a.estado = Estado.PEDIDA;
        return a;
    }

    public static AcumulacaoFuncoes reconstruir(AcumulacaoFuncoesId id, FuncionarioId funcionarioId, Tipo tipo, CasoPublico caso,
                                                boolean remunerada, String entidade, String funcoes, String horario, Integer horasSemanais,
                                                LocalDate inicio, LocalDate fim, boolean declaracao, Estado estado, String despacho,
                                                LocalDate dataDespacho, String motivo, LocalDate dataFimEfectiva) {
        var a = new AcumulacaoFuncoes();
        a.id = id;
        a.funcionarioId = funcionarioId;
        a.tipo = tipo;
        a.casoPublico = caso;
        a.remunerada = remunerada;
        a.entidade = entidade;
        a.funcoes = funcoes;
        a.horario = horario;
        a.horasSemanais = horasSemanais;
        a.inicio = inicio;
        a.fim = fim;
        a.declaracaoSemConflito = declaracao;
        a.estado = estado;
        a.despacho = despacho;
        a.dataDespacho = dataDespacho;
        a.motivo = motivo;
        a.dataFimEfectiva = dataFimEfectiva;
        return a;
    }

    /** Quem autoriza, pelo art. 23.º. */
    public Autorizacao autorizacao() {
        return remunerada ? Autorizacao.MEMBROS_GOVERNO : Autorizacao.DIRIGENTE_MAXIMO;
    }

    public void autorizar(String despacho, LocalDate data) {
        exigir(Estado.PEDIDA, "autorizar");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho que autoriza a acumulação (art. 23.º).");
        this.despacho = despacho.trim();
        this.dataDespacho = data;
        this.estado = Estado.AUTORIZADA;
    }

    public void indeferir(String motivo, LocalDate data) {
        exigir(Estado.PEDIDA, "indeferir");
        if (motivo == null || motivo.isBlank()) throw invalido("Indique o motivo do indeferimento.");
        this.motivo = motivo.trim();
        this.dataDespacho = data;
        this.estado = Estado.INDEFERIDA;
    }

    /** Cessar antes do fim (pelo próprio ou pelo RH — p. ex. por se ter tornado incompatível). */
    public void cessar(LocalDate data, String motivo) {
        exigir(Estado.AUTORIZADA, "cessar");
        if (data == null || data.isBefore(inicio)) throw invalido("A cessação não pode ser antes do início.");
        this.dataFimEfectiva = data;
        this.motivo = motivo != null && !motivo.isBlank() ? motivo.trim() : null;
        this.estado = Estado.CESSADA;
    }

    /** O período terminou: caduca. */
    public boolean caducarSeTerminou(LocalDate hoje) {
        if (estado != Estado.AUTORIZADA || fim == null || !hoje.isAfter(fim)) return false;
        this.dataFimEfectiva = fim;
        this.estado = Estado.CADUCADA;
        return true;
    }

    public boolean emVigor(LocalDate dia) {
        return estado == Estado.AUTORIZADA && !dia.isBefore(inicio) && (fim == null || !dia.isAfter(fim));
    }

    private void exigir(Estado esperado, String accao) {
        if (estado != esperado) throw IgrpResponseStatusException.conflict("Não é possível " + accao + " no estado em que a acumulação está.");
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
