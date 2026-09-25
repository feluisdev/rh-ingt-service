package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * As modalidades de aposentação da Lei n.º 20/X/2023 (e a pena do Estatuto Disciplinar). Fixadas na
 * lei — por isso enum.
 */
public enum ModalidadeAposentacao {
    /** Art. 48.º n.º 1 e art. 93.º al. b): o vínculo cessa aos 65 anos (ou no fim da prorrogação, até aos 70). */
    LIMITE_IDADE,
    /** Art. 175.º: a pedido, com 34 anos de serviço, condicionada ao interesse da Administração. */
    ANTECIPADA_PEDIDO,
    /** Art. 176.º: no interesse da Administração (carreiras do DL de execução orçamental); exige o acordo do funcionário. */
    ANTECIPADA_INTERESSE_ADMINISTRACAO,
    /** Art. 189.º n.os 3 e 4: incapacidade permanente, declarada pela junta. */
    INVALIDEZ,
    /** Art. 179.º: suspensão do vínculo com prestação mensal, até à aposentação; ≥ 58 anos e ≥ 30 de serviço. */
    PRE_APOSENTACAO,
    /** Estatuto Disciplinar, art. 16.º n.º 6: pena de aposentação compulsiva (imediata desligação do serviço). */
    COMPULSIVA;

    /** Art. 178.º: os Lugares deixados pela aposentação antecipada consideram-se extintos. */
    public boolean extingueLugar() {
        return this == ANTECIPADA_PEDIDO || this == ANTECIPADA_INTERESSE_ADMINISTRACAO;
    }

    /** O próprio pode pedir (em {@code /me}): as que são da iniciativa do funcionário. */
    public boolean podeSerPedidaPeloProprio() {
        return this == ANTECIPADA_PEDIDO || this == PRE_APOSENTACAO;
    }

    public static ModalidadeAposentacao de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
