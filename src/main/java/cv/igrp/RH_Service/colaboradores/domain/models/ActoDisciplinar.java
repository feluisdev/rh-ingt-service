package cv.igrp.RH_Service.colaboradores.domain.models;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Um acto do processo disciplinar, com data. É dos actos que se derivam os prazos (ver
 * {@link ProcessoDisciplinar#prazos}); cada um guarda o que o seu prazo precisa ({@code dias}, {@code pena}…).
 */
public record ActoDisciplinar(UUID id, Tipo tipo, LocalDate data, LocalDate dataFim, Integer dias, PenaDisciplinar pena,
                              Integer duracao, String texto) {

    public enum Tipo {
        PARTICIPACAO,
        INSTAURACAO,
        NOMEACAO_INSTRUTOR,
        INICIO_INSTRUCAO,
        PRORROGACAO_INSTRUCAO,
        SUSPENSAO_PREVENTIVA,
        LEVANTAMENTO_SUSPENSAO,
        ACUSACAO,
        NOTIFICACAO_ACUSACAO,
        DEFESA,
        RELATORIO,
        DECISAO,
        NOTIFICACAO_DECISAO,
        RECURSO,
        DECISAO_RECURSO,
        EFEITOS,
        ARQUIVAMENTO
    }

    public static ActoDisciplinar de(Tipo tipo, LocalDate data, String texto) {
        return new ActoDisciplinar(UUID.randomUUID(), tipo, data, null, null, null, null, texto);
    }
}
