package cv.igrp.RH_Service.shared.domain.notificacoes;

/**
 * O que uma notificação anuncia. Serve para o ecrã escolher o ícone e a ligação, e para filtrar a
 * caixa; o texto que o utilizador lê vai sempre no título e no corpo da própria notificação.
 *
 * <p>Um tipo novo acrescenta-se aqui — é o único sítio a mexer para um módulo passar a notificar.
 * Guarda-se pelo nome ({@code t_notificacao.tipo}), por isso <b>não se renomeiam</b> valores já usados.
 */
public enum TipoNotificacao {

    /** Aviso genérico, sem acção associada. */
    AVISO,

    // Ausências
    PEDIDO_AUSENCIA_PENDENTE,
    PEDIDO_AUSENCIA_DECIDIDO,

    // Aposentação e limite de idade
    LIMITE_IDADE_PROXIMO,
    APOSENTACAO_CONDICOES_REUNIDAS,
    PROCESSO_APOSENTACAO,

    // Declarações e certidões
    DECLARACAO_PEDIDA,
    DECLARACAO_EMITIDA,

    // Lista de antiguidade
    LISTA_ANTIGUIDADE_AFIXADA,
    RECLAMACAO_ANTIGUIDADE,

    // Publicações, cartão profissional
    PUBLICACAO_PENDENTE,
    CARTAO_PROFISSIONAL,

    // Entrada ao serviço, concursos, checklists, comissões
    PERIODO_PROVA,
    CONTRATO_TERMO_A_TERMINAR,
    LUGAR_RESERVADO,
    CONCURSO,
    CHECKLIST,
    COMISSAO_SERVICO,

    // Processos
    PROCESSO_DISCIPLINAR,
    AUTO_ASSIDUIDADE,
    FORMACAO,
    MISSAO_SERVICO,
    EXONERACAO,
    ACUMULACAO_FUNCOES,

    // Saúde e segurança no trabalho
    ACIDENTE_SERVICO,
    EXAME_SAUDE,

    // Fecho mensal
    FECHO_MENSAL
}
