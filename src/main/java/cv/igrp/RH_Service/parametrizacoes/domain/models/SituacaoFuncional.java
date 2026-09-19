package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Situações administrativas do funcionário relativamente ao quadro
 * (Lei n.º 20/X/2023, art. 117.º). São seis e estão fechadas na lei, por isso
 * vivem no código como enum; o que é parametrizável é <b>qual</b> a situação de
 * cada estado do catálogo ({@code t_worker_state.situacao_funcional}, V42).
 *
 * <p>Os efeitos não se guardam em colunas porque são <b>derivados</b> da situação
 * pela própria lei: a inactividade fora do quadro abre vaga (art. 121.º n.º 2) e
 * a inactividade no quadro não conta para antiguidade (art. 120.º n.º 2). Guardar
 * isso em dados seria permitir configurar algo que o legislador já fixou.
 *
 * <p>Fora deste enum ficam os estados de <b>cessação</b> que não são aposentação
 * (exoneração, caducidade, mútuo acordo…): quem cessa deixa de ter situação
 * perante o quadro. Esses estados ficam com situação nula e marcados com
 * {@code ends_employment} (V40).
 */
public enum SituacaoFuncional {

    /** Art. 118.º — desempenha funções, ou está ausente por motivo legalmente justificado. */
    ACTIVIDADE_NO_QUADRO,

    /** Art. 119.º — comissão de serviço, requisição, cedência, cargos políticos. */
    ACTIVIDADE_FORA_QUADRO,

    /** Art. 120.º — licença de 30 dias a 3 anos, incapacidade temporária, suspensão disciplinar. */
    INACTIVIDADE_NO_QUADRO,

    /** Art. 121.º — licença sem vencimento de longa duração, pena de inactividade, doença &gt; 30 dias. */
    INACTIVIDADE_FORA_QUADRO,

    /** Art. 122.º — aguarda vaga na sua categoria, com contagem de tempo e abonos. */
    DISPONIBILIDADE,

    /** Art. 117.º al. f — aposentação; cessa o vínculo (art. 93.º al. b). */
    APOSENTACAO;

    /**
     * A situação liberta o Lugar que o funcionário ocupa?
     *
     * <p>Só a inactividade fora do quadro o faz por si (art. 121.º n.º 2). A
     * abertura de vaga da actividade no quadro (art. 118.º n.º 2) depende do
     * motivo e da duração da ausência — cônjuge diplomata além de um ano,
     * formação no exterior além de seis meses — e por isso pertence ao subtipo
     * da licença, não ao estado.
     */
    public boolean abreVaga() {
        return this == INACTIVIDADE_FORA_QUADRO;
    }

    /**
     * O tempo nesta situação conta para a antiguidade?
     *
     * <p>Art. 120.º n.º 2: o tempo de inactividade no quadro não conta. A
     * inactividade fora do quadro suspende o vínculo, logo também não. A
     * disponibilidade conta expressamente (art. 122.º n.º 1).
     */
    public boolean contaAntiguidade() {
        return this != INACTIVIDADE_NO_QUADRO && this != INACTIVIDADE_FORA_QUADRO;
    }

    /**
     * A situação suspende a execução do contrato? Nas duas inactividades o
     * funcionário não exerce funções; na aposentação o vínculo cessa, e a
     * cessação tem caminho próprio ({@code CessacaoService}).
     */
    public boolean suspendeVinculo() {
        return this == INACTIVIDADE_NO_QUADRO || this == INACTIVIDADE_FORA_QUADRO;
    }

    /**
     * O titular nesta situação pode ser <b>substituído</b> no seu Lugar?
     *
     * <p>São as duas situações em que o funcionário <b>mantém o Lugar mas não
     * exerce</b>: a actividade fora do quadro (art. 119.º — comissão, requisição,
     * cedência) e a inactividade no quadro (art. 120.º — licença até três anos,
     * incapacidade temporária, suspensão disciplinar). É exactamente o
     * "temporariamente impedido" do art. 73.º al. a) a c).
     *
     * <p>Quem está em actividade no quadro exerce, e não há nada a substituir. Nas
     * restantes situações o funcionário já <b>não tem</b> Lugar — a inactividade
     * fora do quadro abre vaga, a disponibilidade é estar sem Lugar, e a
     * aposentação cessa o vínculo —, e um Lugar vago provê-se com titular, não
     * com substituto.
     */
    public boolean permiteSubstituicao() {
        return this == ACTIVIDADE_FORA_QUADRO || this == INACTIVIDADE_NO_QUADRO;
    }

    /** A situação termina a relação de emprego público? Só a aposentação. */
    public boolean cessaVinculo() {
        return this == APOSENTACAO;
    }

    /** Converte o valor guardado, recusando o que não é uma das seis situações da lei. */
    public static SituacaoFuncional de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return SituacaoFuncional.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Situação funcional inválida: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }
}
