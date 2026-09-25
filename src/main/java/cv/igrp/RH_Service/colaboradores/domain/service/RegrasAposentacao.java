package cv.igrp.RH_Service.colaboradores.domain.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Os números da aposentação que a Lei n.º 20/X/2023 fixa, e as datas que deles se derivam. Não se
 * parametrizam: são da lei (BR-APO-01..04).
 *
 * <p>O tempo de serviço projecta-se a partir dos dias contados hoje (a antiguidade, já com os
 * descontos), com o ano de 365 dias do art. 70.º n.º 1 do DL n.º 3/2010, e <b>sem</b> supor descontos
 * futuros — é uma previsão, e diz-se como tal.
 */
public final class RegrasAposentacao {

    /** Art. 48.º n.º 1: o vínculo cessa aos 65 anos. */
    public static final int IDADE_LIMITE = 65;
    /** Art. 48.º n.º 2 e art. 96.º n.º 2 c): por interesse público excepcional, até aos 70. */
    public static final int IDADE_MAXIMA = 70;
    /** Art. 175.º n.º 1: aposentação antecipada a pedido com 34 anos de serviço. */
    public static final int ANOS_SERVICO_ANTECIPADA = 34;
    /** Art. 179.º n.º 2: pré-aposentação com 58 anos de idade... */
    public static final int IDADE_PRE_APOSENTACAO = 58;
    /** ... e 30 de serviço. */
    public static final int ANOS_SERVICO_PRE_APOSENTACAO = 30;
    /** Art. 179.º n.º 4: a prestação fica entre 70% e 80% da remuneração base. */
    public static final BigDecimal PRESTACAO_MINIMA = BigDecimal.valueOf(70);
    public static final BigDecimal PRESTACAO_MAXIMA = BigDecimal.valueOf(80);
    /** DL n.º 3/2010, art. 70.º n.º 1: o ano de antiguidade tem 365 dias. */
    public static final int DIAS_ANO = 365;

    private RegrasAposentacao() {}

    public static LocalDate faz(LocalDate nascimento, int anos) {
        return nascimento == null ? null : nascimento.plusYears(anos);
    }

    public static int idade(LocalDate nascimento, LocalDate em) {
        if (nascimento == null || em == null || em.isBefore(nascimento)) return 0;
        return java.time.Period.between(nascimento, em).getYears();
    }

    /**
     * O dia em que o tempo de serviço chega a {@code anos}, sabendo que em {@code hoje} já conta
     * {@code diasContados}. Se já lá chegou, o dia (aproximado) em que chegou.
     */
    public static LocalDate completaAnosDeServico(int anos, long diasContados, LocalDate hoje) {
        long faltam = (long) anos * DIAS_ANO - diasContados;
        return hoje.plusDays(faltam);
    }

    /** A pré-aposentação é possível no dia em que as duas condições se verificam. */
    public static LocalDate preAposentacaoPossivel(LocalDate nascimento, long diasContados, LocalDate hoje) {
        if (nascimento == null) return null;
        LocalDate idade = faz(nascimento, IDADE_PRE_APOSENTACAO);
        LocalDate servico = completaAnosDeServico(ANOS_SERVICO_PRE_APOSENTACAO, diasContados, hoje);
        return idade.isAfter(servico) ? idade : servico;
    }

    public static boolean prestacaoValida(BigDecimal percentagem) {
        return percentagem != null && percentagem.compareTo(PRESTACAO_MINIMA) >= 0
                && percentagem.compareTo(PRESTACAO_MAXIMA) <= 0;
    }
}
