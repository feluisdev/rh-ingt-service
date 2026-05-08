package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class LicencaMobilidade {

    private LicencaMobilidadeId id;
    private FuncionarioId funcionarioId;
    private SubtipoLicencaMobilidadeId subtipoId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String entidadeDestino;
    private String despachoNumero;
    private String observacoes;
    private Boolean isActive;
    private String status;
    private UUID destinationUnitId;
    private String justification;
    private UUID documentId;
    private String rejectionReason;

    private LicencaMobilidade() {}

    public static LicencaMobilidade criar(FuncionarioId funcionarioId,
                                          SubtipoLicencaMobilidadeId subtipoId,
                                          LocalDate dataInicio, LocalDate dataFim,
                                          String entidadeDestino, String despachoNumero,
                                          String observacoes, String justification,
                                          UUID destinationUnitId, UUID documentId) {
        LicencaMobilidade l = new LicencaMobilidade();
        l.id = LicencaMobilidadeId.gerarNovo();
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.justification = justification;
        l.destinationUnitId = destinationUnitId;
        l.documentId = documentId;
        l.isActive = true;
        l.status = "PENDING";
        return l;
    }

    public static LicencaMobilidade reconstituir(LicencaMobilidadeId id,
                                                  FuncionarioId funcionarioId,
                                                  SubtipoLicencaMobilidadeId subtipoId,
                                                  LocalDate dataInicio, LocalDate dataFim,
                                                  String entidadeDestino, String despachoNumero,
                                                  String observacoes, Boolean isActive,
                                                  String status, UUID destinationUnitId,
                                                  String justification, UUID documentId,
                                                  String rejectionReason) {
        LicencaMobilidade l = new LicencaMobilidade();
        l.id = id;
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.isActive = isActive;
        l.status = status != null ? status : "PENDING";
        l.destinationUnitId = destinationUnitId;
        l.justification = justification;
        l.documentId = documentId;
        l.rejectionReason = rejectionReason;
        return l;
    }

    public void atualizar(LocalDate dataInicio, LocalDate dataFim, String entidadeDestino,
                          String despachoNumero, String observacoes) {
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.entidadeDestino = entidadeDestino;
        this.despachoNumero = despachoNumero;
        this.observacoes = observacoes;
    }

    public void ativar() {
        this.isActive = true;
        this.status = "ACTIVE";
    }

    public void desativar() {
        this.isActive = false;
    }

    public void aprovar() {
        this.isActive = true;
        this.status = "ACTIVE";
    }

    public void rejeitar(String reason) {
        this.isActive = false;
        this.status = "REJECTED";
        this.rejectionReason = reason;
    }

    public void encerrar() {
        this.isActive = false;
        this.status = "CLOSED";
        if (this.dataFim == null) this.dataFim = LocalDate.now();
    }

    public void cancelar() {
        this.isActive = false;
        this.status = "CANCELLED";
    }

    public void associarDocumento(UUID documentId) {
        this.documentId = documentId;
    }

    public boolean isPending() { return "PENDING".equals(this.status); }
    public boolean isApproved() { return "ACTIVE".equals(this.status); }
}
