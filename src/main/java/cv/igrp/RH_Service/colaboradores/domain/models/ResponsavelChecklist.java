package cv.igrp.RH_Service.colaboradores.domain.models;

/**
 * Quem trata de um item da checklist [ind.]: a lista de trabalho de cada área filtra-se por aqui. O próprio vê os
 * seus itens em {@code /me/checklists}.
 */
public enum ResponsavelChecklist {
    RH,
    CHEFIA,
    PROPRIO,
    INFORMATICA,
    PATRIMONIO
}
