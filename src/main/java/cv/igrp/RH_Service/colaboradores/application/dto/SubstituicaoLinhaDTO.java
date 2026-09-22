package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * Uma substituição, vista do lado de um colaborador — art. 91.º n.º 1 al. a) da Lei
 * n.º 20/X/2023.
 *
 * <p>A mesma linha serve os dois papéis: quem substitui e quem está a ser substituído. O
 * {@code papel} diz qual deles o colaborador da consulta desempenha, e o bloco do
 * <i>contraparte</i> traz o outro — que é o que um ecrã de RH precisa para dizer «quem substitui
 * quem».
 */
@Data
public class SubstituicaoLinhaDTO {

    /** A afectação de substituição. */
    private String id;

    /** {@code SUBSTITUTO} ou {@code TITULAR}, conforme o papel de quem foi consultado. */
    private String papel;

    /** O outro lado: quem substitui, se a consulta é do titular; o titular, se é do substituto. */
    private String contraparteId;
    private String contraparteNome;
    private String contraparteNumero;

    /** O Lugar coberto. */
    private String positionId;
    private String numeroLugar;
    private String unidadeOrganicaId;
    private String unidadeOrganicaNome;

    private LocalDate dataInicio;
    /**
     * Nulo enquanto durar. A substituição <b>não tem data de fim combinada</b>: caduca quando o
     * titular regressa (art. 77.º n.º 2), e é então que esta data é preenchida.
     */
    private LocalDate dataFim;

    /** Está em vigor hoje. */
    private boolean corrente;

    private String observacoes;
}
