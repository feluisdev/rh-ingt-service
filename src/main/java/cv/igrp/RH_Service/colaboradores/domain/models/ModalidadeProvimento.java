package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * As formas de vinculação da Lei n.º 20/X/2023 (arts. 52.º–78.º): nomeação (provisória, definitiva, em
 * comissão de serviço), contrato de gestão e contrato de trabalho (estágio, tempo indeterminado, a termo).
 * Fixadas na lei — enum.
 */
public enum ModalidadeProvimento {
    /** Art. 57.º: por tempo determinado, para o estágio probatório (funções próprias do Estado). */
    NOMEACAO_PROVISORIA,
    /** Art. 58.º: por tempo indeterminado, depois do estágio com sucesso (ou dispensado). */
    NOMEACAO_DEFINITIVA,
    /** Arts. 59.º–64.º: dirigentes, cargos de livre escolha; 3 anos renováveis. */
    COMISSAO_SERVICO,
    /** Arts. 65.º–68.º: função dirigente ou de quadro especial; 3 anos renováveis. */
    CONTRATO_GESTAO,
    /** Art. 72.º: o estágio probatório de quem vai ter contrato por tempo indeterminado; 1 ano. */
    CONTRATO_ESTAGIO,
    /** Art. 70.º: contrato de trabalho por tempo indeterminado. */
    CONTRATO_INDETERMINADO,
    /** Arts. 73.º–80.º: a termo resolutivo certo — com período experimental. */
    CONTRATO_TERMO_CERTO,
    /** A termo resolutivo incerto — com período experimental. */
    CONTRATO_TERMO_INCERTO;

    /** Começa por um estágio probatório (arts. 57.º e 72.º). */
    public boolean temEstagioProbatorio() {
        return this == NOMEACAO_PROVISORIA || this == CONTRATO_ESTAGIO;
    }

    /** Começa por um período experimental (arts. 79.º e 80.º). */
    public boolean temPeriodoExperimental() {
        return this == CONTRATO_TERMO_CERTO || this == CONTRATO_TERMO_INCERTO;
    }

    /** A modalidade para onde passa quem conclui o estágio com sucesso (art. 58.º n.º 1; art. 72.º n.º 1). */
    public ModalidadeProvimento depoisDoEstagio() {
        return switch (this) {
            case NOMEACAO_PROVISORIA -> NOMEACAO_DEFINITIVA;
            case CONTRATO_ESTAGIO -> CONTRATO_INDETERMINADO;
            default -> null;
        };
    }

    public static ModalidadeProvimento de(String valor) {
        if (valor == null || valor.isBlank()) return null;
        try {
            return valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
