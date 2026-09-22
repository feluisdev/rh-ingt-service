package cv.igrp.RH_Service.parametrizacoes.domain.models;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * O que acontece ao funcionário quando a licença termina.
 *
 * <p>Quem manteve o Lugar regressa a ele. Quem o perdeu tem direito a uma vaga
 * existente ou à primeira que ocorra (DL n.º 3/2010, art. 50.º a 53.º), e é isso
 * a disponibilidade da Lei n.º 20/X/2023, art. 122.º: aguarda vaga, com contagem
 * de tempo e abonos, e com precedência sobre transferências (n.º 4).
 */
public enum EfeitoNoRegresso {

    /** Volta ao Lugar de que nunca deixou de ser titular. */
    REGRESSA_LUGAR,

    /** Fica a aguardar vaga na sua categoria (art. 122.º). */
    DISPONIBILIDADE,

    /**
     * <b>Regressa — ou cessa, se não tiver para onde regressar</b> (Lei n.º 20/X/2023,
     * art. 64.º n.º 2): «Cessada a comissão de serviço, o nomeado regressa à situação
     * jurídico-funcional de que era titular antes dela, quando constituída e consolidada por
     * tempo indeterminado, ou, no caso contrário, cessa a relação jurídica de emprego
     * público.»
     *
     * <p>É o único efeito no regresso que pode <b>terminar</b> o vínculo, e serve o caso de quem
     * foi recrutado <i>para</i> a comissão e nunca teve Lugar do quadro: acabada a comissão,
     * não há situação anterior a que voltar.
     *
     * <p><b>Qual dos dois caminhos se segue deriva-se do percurso</b>, não de um campo que
     * alguém preencha: a comissão mantém o Lugar, logo quem tinha Lugar continua a tê-lo e
     * regressa; quem não tem nenhum nunca teve situação para onde voltar.
     */
    REGRESSA_OU_CESSA;

    public static EfeitoNoRegresso de(String valor) {
        if (valor == null || valor.isBlank()) return REGRESSA_LUGAR;
        try {
            return EfeitoNoRegresso.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Efeito no regresso inválido: '" + valor + "'. Valores aceites: "
                            + Arrays.stream(values()).map(Enum::name).collect(Collectors.joining(", ")) + ".");
        }
    }
}
