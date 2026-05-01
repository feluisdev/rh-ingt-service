package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.LicencaMobilidadeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SubtipoLicencaMobilidadeId;
import lombok.Getter;

import java.time.LocalDate;

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

    private LicencaMobilidade() {}

    public static LicencaMobilidade criar(FuncionarioId funcionarioId,
                                          SubtipoLicencaMobilidadeId subtipoId,
                                          LocalDate dataInicio, LocalDate dataFim,
                                          String entidadeDestino, String despachoNumero,
                                          String observacoes) {
        LicencaMobilidade l = new LicencaMobilidade();
        l.id = LicencaMobilidadeId.gerarNovo();
        l.funcionarioId = funcionarioId;
        l.subtipoId = subtipoId;
        l.dataInicio = dataInicio;
        l.dataFim = dataFim;
        l.entidadeDestino = entidadeDestino;
        l.despachoNumero = despachoNumero;
        l.observacoes = observacoes;
        l.isActive = true;
        return l;
    }

    public static LicencaMobilidade reconstituir(LicencaMobilidadeId id,
                                                  FuncionarioId funcionarioId,
                                                  SubtipoLicencaMobilidadeId subtipoId,
                                                  LocalDate dataInicio, LocalDate dataFim,
                                                  String entidadeDestino, String despachoNumero,
                                                  String observacoes, Boolean isActive) {
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

    public void ativar() { this.isActive = true; }
    public void desativar() { this.isActive = false; }
}
