package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.LocalDate;

/**
 * <b>A escala das penas</b> (Estatuto Disciplinar, art. 14.º), com a gravidade para as comparações da lei
 * («suspensão ou superior», «igual ou superior à multa») e a prescrição do procedimento (art. 6.º n.º 1).
 */
public enum PenaDisciplinar {
    /** Art. 16.º n.º 1: mera advertência. */
    CENSURA_ESCRITA(1),
    /** Art. 16.º n.º 2: até 20 dias de remunerações (o valor é do salarial; aqui, os dias). */
    MULTA(2),
    /** Art. 16.º n.º 4: 21 a 121 dias de afastamento. */
    SUSPENSAO(3),
    /** Art. 16.º n.º 5: 6 a 18 meses de afastamento. */
    INACTIVIDADE(4),
    /** Art. 16.º n.º 6: passagem compulsiva à aposentação. */
    APOSENTACAO_COMPULSIVA(5),
    /** Art. 16.º n.º 7: afastamento definitivo, cessa o vínculo. */
    DEMISSAO(6),
    /**
     * Art. 14.º n.º 2 e art. 16.º n.º 8: dirigentes e equiparados. Não entra na escala do n.º 1; para a prescrição e
     * para «igual ou superior à multa» vale como a multa [interp.].
     */
    CESSACAO_COMISSAO(2);

    private final int gravidade;

    PenaDisciplinar(int gravidade) {
        this.gravidade = gravidade;
    }

    public boolean pelomenos(PenaDisciplinar outra) {
        return gravidade >= outra.gravidade;
    }

    /** As que afastam o agente durante um período (art. 16.º n.º 3). */
    public boolean temPeriodo() {
        return this == SUSPENSAO || this == INACTIVIDADE;
    }

    /** As que acabam com o exercício de funções (ou do cargo) — executam-se só depois de passar o prazo de recurso [interp.]. */
    public boolean expulsiva() {
        return this == APOSENTACAO_COMPULSIVA || this == DEMISSAO || this == CESSACAO_COMISSAO;
    }

    /** Publicadas no Boletim Oficial (art. 15.º n.º 2). */
    public boolean publica() {
        return this == APOSENTACAO_COMPULSIVA || this == DEMISSAO;
    }

    /** Art. 6.º n.º 1: 6 meses (censura), 2 anos (multa, suspensão, inactividade), 3 anos (aposentação, demissão). */
    public LocalDate prescricao(LocalDate infraccao) {
        return switch (this) {
            case CENSURA_ESCRITA -> infraccao.plusMonths(6);
            case APOSENTACAO_COMPULSIVA, DEMISSAO -> infraccao.plusYears(3);
            default -> infraccao.plusYears(2);
        };
    }

    /**
     * A duração que a pena exige (art. 16.º): multa até 20 dias, suspensão 21 a 121 dias, inactividade 6 a 18 meses; as
     * outras não têm. Devolve a mensagem do que está mal, ou {@code null}.
     */
    public String validarDuracao(Integer duracao) {
        return switch (this) {
            case MULTA -> duracao == null || duracao < 1 || duracao > 20 ? "A multa vai de 1 a 20 dias de remuneração." : null;
            case SUSPENSAO -> duracao == null || duracao < 21 || duracao > 121 ? "A suspensão vai de 21 a 121 dias." : null;
            case INACTIVIDADE -> duracao == null || duracao < 6 || duracao > 18 ? "A inactividade vai de 6 a 18 meses." : null;
            default -> duracao != null ? "Esta pena não tem duração." : null;
        };
    }

    /** O último dia de uma pena com período, a começar em {@code inicio}. */
    public LocalDate fim(LocalDate inicio, Integer duracao) {
        return switch (this) {
            case SUSPENSAO -> inicio.plusDays(duracao - 1L);
            case INACTIVIDADE -> inicio.plusMonths(duracao).minusDays(1);
            default -> null;
        };
    }

    public String nome() {
        return switch (this) {
            case CENSURA_ESCRITA -> "censura escrita";
            case MULTA -> "multa";
            case SUSPENSAO -> "suspensão";
            case INACTIVIDADE -> "inactividade";
            case APOSENTACAO_COMPULSIVA -> "aposentação compulsiva";
            case DEMISSAO -> "demissão";
            case CESSACAO_COMISSAO -> "cessação da comissão de serviço";
        };
    }
}
