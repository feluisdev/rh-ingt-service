package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;
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
    private String estadoMotivo;    // porque está no estado actual (congelar/descongelar)
    private String estadoDespacho;
    private LocalDate estadoDesde;
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

    /**
     * Reclassifica o Lugar para outra carreira/categoria — é o que acontece numa promoção
     * em que o ocupante <b>fica na mesma cadeira</b> e é a cadeira que sobe de categoria.
     * Altera o Mapa de Pessoal: a partir daqui o Lugar pertence à nova categoria, mesmo
     * depois de o ocupante sair.
     */
    public void reclassificarPara(UUID careerId, UUID categoryId) {
        if (!podeSerOcupado()) {
            throw IgrpResponseStatusException.conflict(
                    "Só um Lugar activo (estado ATIVO) pode ser reclassificado. Estado actual: " + this.estado);
        }
        this.careerId = careerId;
        this.categoryId = categoryId;
    }

    public boolean isForaDeGrelha() {
        return this.careerId == null || this.categoryId == null;
    }

    /**
     * Congela o Lugar: sai da dotação e deixa de poder ser ocupado. É um acto administrativo
     * (falta de dotação, reestruturação) e exige motivo; fica o motivo, o despacho e a data.
     * Quem chama garante que o Lugar não tem titular — a ocupação é de colaboradores.
     * Um Lugar que não pode voltar não se congela: extingue-se.
     *
     * @return {@code false} se já estava congelado (nada a fazer)
     */
    public boolean congelar(String motivo, String despachoNumero, LocalDate data) {
        if (EXTINTO.equals(this.estado)) {
            throw IgrpResponseStatusException.conflict("Este Lugar foi extinto e não pode ser congelado.");
        }
        if (CONGELADO.equals(this.estado)) {
            return false;
        }
        registarMotivo(motivo, despachoNumero, data);
        this.estado = CONGELADO;
        return true;
    }

    /**
     * Descongela: o Lugar volta a {@code ATIVO} e à dotação. Também exige motivo.
     *
     * @return {@code false} se já estava activo (nada a fazer)
     */
    public boolean descongelar(String motivo, String despachoNumero, LocalDate data) {
        if (EXTINTO.equals(this.estado)) {
            throw IgrpResponseStatusException.conflict("Este Lugar foi extinto e não pode voltar a estar activo.");
        }
        if (ATIVO.equals(this.estado)) {
            return false;
        }
        registarMotivo(motivo, despachoNumero, data);
        this.estado = ATIVO;
        return true;
    }

    private void registarMotivo(String motivo, String despachoNumero, LocalDate data) {
        if (motivo == null || motivo.isBlank()) {
            throw IgrpResponseStatusException.badRequest("Indique o motivo.");
        }
        this.estadoMotivo = motivo.trim();
        this.estadoDespacho = despachoNumero == null || despachoNumero.isBlank() ? null : despachoNumero.trim();
        this.estadoDesde = data;
    }

    /** Só para a persistência: repõe o motivo gravado com o estado. */
    public void reconstituirMotivoDoEstado(String motivo, String despachoNumero, LocalDate desde) {
        this.estadoMotivo = motivo;
        this.estadoDespacho = despachoNumero;
        this.estadoDesde = desde;
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
