package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.UUID;

/**
 * Lugar (Position) — uma cadeira no Mapa de Pessoal. Existe por si, mesmo vaga.
 * O estado de ocupação (PROVIDO/VAGO) NÃO vive aqui — deriva-se da afectação
 * corrente. Aqui guarda-se apenas o estado administrativo do Lugar.
 */
@Getter
public class Position {

    public static final String ATIVO = "ATIVO";
    public static final String CONGELADO = "CONGELADO";
    public static final String EXTINTO = "EXTINTO";

    private PositionId id;
    private String numeroLugar;
    private UUID jobId;
    private UUID unidadeOrganicaId;
    private UUID careerId;          // null = fora de grelha (tarefeiro/prestador)
    private UUID categoryId;        // null = fora de grelha
    private UUID parentPositionId;  // reporte estrutural (chefia)
    private UUID managesUnitId;     // unidade que este Lugar dirige (responsável)
    private String estado;
    private String legalBase;
    private boolean active;

    private Position() {}

    public static Position criar(String numeroLugar, UUID jobId, UUID unidadeOrganicaId,
                                 UUID careerId, UUID categoryId, UUID parentPositionId,
                                 UUID managesUnitId, String legalBase) {
        Position p = new Position();
        p.id = PositionId.gerarNovo();
        p.numeroLugar = numeroLugar;
        p.jobId = jobId;
        p.unidadeOrganicaId = unidadeOrganicaId;
        p.careerId = careerId;
        p.categoryId = categoryId;
        p.parentPositionId = parentPositionId;
        p.managesUnitId = managesUnitId;
        p.estado = ATIVO;
        p.legalBase = legalBase;
        p.active = true;
        return p;
    }

    public static Position reconstituir(PositionId id, String numeroLugar, UUID jobId,
                                        UUID unidadeOrganicaId, UUID careerId, UUID categoryId,
                                        UUID parentPositionId, UUID managesUnitId, String estado,
                                        String legalBase, boolean active) {
        Position p = new Position();
        p.id = id;
        p.numeroLugar = numeroLugar;
        p.jobId = jobId;
        p.unidadeOrganicaId = unidadeOrganicaId;
        p.careerId = careerId;
        p.categoryId = categoryId;
        p.parentPositionId = parentPositionId;
        p.managesUnitId = managesUnitId;
        p.estado = estado;
        p.legalBase = legalBase;
        p.active = active;
        return p;
    }

    public void atualizar(UUID jobId, UUID unidadeOrganicaId, UUID careerId, UUID categoryId,
                          UUID parentPositionId, UUID managesUnitId, String legalBase) {
        this.jobId = jobId;
        this.unidadeOrganicaId = unidadeOrganicaId;
        this.careerId = careerId;
        this.categoryId = categoryId;
        this.parentPositionId = parentPositionId;
        this.managesUnitId = managesUnitId;
        this.legalBase = legalBase;
    }

    public boolean isForaDeGrelha() {
        return this.careerId == null || this.categoryId == null;
    }

    public void congelar() {
        if (EXTINTO.equals(this.estado)) {
            throw IgrpResponseStatusException.conflict("Um Lugar extinto não pode ser congelado.");
        }
        this.estado = CONGELADO;
    }

    public void reativarEstado() {
        if (EXTINTO.equals(this.estado)) {
            throw IgrpResponseStatusException.conflict("Um Lugar extinto não pode voltar a ATIVO.");
        }
        this.estado = ATIVO;
    }

    public void extinguir() {
        this.estado = EXTINTO;
        this.active = false;
    }

    public boolean podeSerOcupado() {
        return this.active && ATIVO.equals(this.estado);
    }

    public void definirChefiaDe(UUID unidadeId) {
        this.managesUnitId = unidadeId;
    }

    public void removerChefia() {
        this.managesUnitId = null;
    }
}
