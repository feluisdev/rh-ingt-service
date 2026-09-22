package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

/**
 * O que a ausência faz à <b>remuneração</b> — art. 16.º do DL n.º 3/2010, e art. 43.º n.º 2 para
 * a falta injustificada.
 *
 * <p><b>Esta aplicação não calcula remuneração</b>, e não é isso que esta classificação faz. O
 * cálculo do art. 16.º n.º 3 — «a diferença entre a remuneração líquida a que o funcionário teria
 * direito e o subsídio pago pela previdência social» — é aritmética sobre valores que aqui não
 * existem, e pertence a quem processa vencimentos. O que o RH tem de saber dizer é <b>qual dos
 * regimes se aplica</b>, para que esse sistema o possa ler junto com os dias da ausência.
 *
 * <p><b>Porque não é um booleano.</b> O {@code affects_pay} do subtipo de licença é um sim/não, e
 * por isso não sabe dizer a diferença entre perder tudo e perder a diferença para o subsídio —
 * que são coisas distintas para quem paga, e a lei distingue-as em números seguidos do mesmo
 * artigo.
 *
 * <p>Como em {@link RegimeAusencia} e em {@link SituacaoFuncional}: a instituição <b>mapeia</b> as
 * suas linhas nos valores que a lei conhece, sem lhes poder inventar um novo.
 */
public enum EfeitoNaRemuneracao {

    /**
     * Regra das faltas justificadas (art. 16.º n.º 1): não interrompem a efectividade do serviço
     * «nem determinam a perda de remunerações ou de quaisquer direitos ou regalias».
     */
    SEM_PERDA,

    /**
     * Perda <b>parcial</b> (art. 16.º n.º 2 e 3) — als. d), e), i), j) e t) do art. 15.º n.º 1:
     * doença, acidente de serviço, assistência a familiar e parentalidade. Há direito aos
     * subsídios da previdência social, e o que se perde é a diferença. Quanto é, não é connosco.
     */
    PERDA_PARCIAL,

    /**
     * Perda <b>total</b> das remunerações dos dias de ausência. É o caso da greve (art. 16.º
     * n.º 4) — que perde remuneração mas, note-se, <b>não desconta antiguidade</b>: são dois
     * eixos independentes, e é por isso que esta classificação não decide nada sobre o tempo de
     * serviço.
     */
    PERDA_TOTAL,

    /**
     * Perda do <b>vencimento de exercício</b> (art. 16.º n.º 5), por prisão preventiva. Não é
     * perda total: na estrutura remuneratória da função pública o vencimento de exercício é uma
     * parcela. E é <b>reparável</b> — o n.º 6 manda repô-lo em caso de revogação, absolvição ou
     * condenação em pena diversa da prisão efectiva.
     */
    PERDA_VENCIMENTO_EXERCICIO,

    /**
     * A lei <b>não fixa</b> o efeito: dá uma opção. É a falta injustificada do art. 43.º n.º 2,
     * que «implica a opção entre a perda das remunerações correspondentes aos dias de ausência,
     * ou o seu desconto nas férias».
     *
     * <p>Qual das duas foi exercida é de <b>cada caso</b>, e fica registada no pedido. Classificar
     * o tipo com uma delas seria decidir por quem tem de escolher.
     */
    DEPENDE_DA_OPCAO;

    /** Por omissão {@link #SEM_PERDA}: afirmar que não há perda nunca tira dinheiro a ninguém. */
    public static EfeitoNaRemuneracao de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return EfeitoNaRemuneracao.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Efeito na remuneração inválido: '" + valor + "'. Valores possíveis: "
                            + "SEM_PERDA, PERDA_PARCIAL, PERDA_TOTAL, PERDA_VENCIMENTO_EXERCICIO, "
                            + "DEPENDE_DA_OPCAO.");
        }
    }

    /** Há alguma coisa a comunicar a quem processa vencimentos? */
    public boolean temEfeito() {
        return this != SEM_PERDA;
    }
}
