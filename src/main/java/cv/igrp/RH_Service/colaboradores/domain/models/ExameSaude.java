package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExameSaudeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

/**
 * <b>Um exame de medicina do trabalho</b> (Lei n.º 20/X/2023, art. 45.º n.º 1 d): a aptidão física e psíquica é requisito;
 * BR-SST-11..15). Só o <b>resultado de aptidão</b> — sem dados clínicos. Tem validade: o periódico seguinte [ind.] é a 2 anos
 * até aos 50 anos de idade e a 1 ano depois; o inapto temporário é reavaliado na data marcada.
 */
@Getter
public class ExameSaude {

    public enum Tipo { ADMISSAO, PERIODICO, OCASIONAL, REGRESSO }
    public enum Resultado { APTO, APTO_CONDICIONADO, INAPTO_TEMPORARIO, INAPTO_DEFINITIVO }

    /** A partir desta idade, o exame periódico é anual [ind.]. */
    public static final int IDADE_ANUAL = 50;

    private ExameSaudeId id;
    private FuncionarioId funcionarioId;
    private Tipo tipo;
    private LocalDate data;
    private String entidade;
    private Resultado resultado;
    private String restricoes;
    private LocalDate validadeAte;
    private String observacoes;

    private ExameSaude() {}

    /**
     * {@code validade} explícita, ou a por omissão: apto e apto condicionado pela idade; inapto temporário exige a data de
     * reavaliação; inapto definitivo não tem validade (segue para a junta).
     */
    public static ExameSaude registar(FuncionarioId funcionarioId, Tipo tipo, LocalDate data, String entidade, Resultado resultado,
                                      String restricoes, LocalDate validade, String observacoes, Integer idade, LocalDate hoje) {
        if (tipo == null) throw invalido("Indique o tipo de exame.");
        if (resultado == null) throw invalido("Indique o resultado de aptidão.");
        if (data == null || data.isAfter(hoje)) throw invalido("Indique a data do exame (não futura).");
        if (resultado == Resultado.APTO_CONDICIONADO && (restricoes == null || restricoes.isBlank()))
            throw invalido("Apto condicionado: indique as restrições (sem dados clínicos).");
        if (resultado == Resultado.INAPTO_TEMPORARIO && validade == null)
            throw invalido("Inapto temporário: indique a data da reavaliação.");
        if (validade != null && !validade.isAfter(data)) throw invalido("A validade é depois do exame.");
        var e = new ExameSaude();
        e.id = ExameSaudeId.gerarNovo();
        e.funcionarioId = funcionarioId;
        e.tipo = tipo;
        e.data = data;
        e.entidade = texto(entidade);
        e.resultado = resultado;
        e.restricoes = texto(restricoes);
        e.validadeAte = validade != null ? validade : resultado == Resultado.INAPTO_DEFINITIVO ? null
                : data.plusYears(idade != null && idade >= IDADE_ANUAL ? 1 : 2);
        e.observacoes = texto(observacoes);
        return e;
    }

    public static ExameSaude reconstruir(ExameSaudeId id, FuncionarioId funcionarioId, Tipo tipo, LocalDate data, String entidade,
                                         Resultado resultado, String restricoes, LocalDate validadeAte, String observacoes) {
        var e = new ExameSaude();
        e.id = id;
        e.funcionarioId = funcionarioId;
        e.tipo = tipo;
        e.data = data;
        e.entidade = entidade;
        e.resultado = resultado;
        e.restricoes = restricoes;
        e.validadeAte = validadeAte;
        e.observacoes = observacoes;
        return e;
    }

    public boolean apto() {
        return resultado == Resultado.APTO || resultado == Resultado.APTO_CONDICIONADO;
    }

    public boolean validoEm(LocalDate dia) {
        return validadeAte == null || !dia.isAfter(validadeAte);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
