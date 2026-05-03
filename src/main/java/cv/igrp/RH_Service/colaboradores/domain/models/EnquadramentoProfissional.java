package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class EnquadramentoProfissional {

    private EnquadramentoId id;
    private FuncionarioId funcionarioId;
    private UUID careerId;
    private UUID categoryId;
    private UUID gradeId;
    private UUID cargoId;
    private UUID unidadeOrganicaId;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private Boolean isCurrent;

    private EnquadramentoProfissional() {}

    public static EnquadramentoProfissional criar(FuncionarioId funcionarioId, UUID careerId,
                                                   UUID categoryId, UUID gradeId, UUID cargoId,
                                                   UUID unidadeOrganicaId, LocalDate dataInicio) {
        EnquadramentoProfissional e = new EnquadramentoProfissional();
        e.id = EnquadramentoId.gerarNovo();
        e.funcionarioId = funcionarioId;
        e.careerId = careerId;
        e.categoryId = categoryId;
        e.gradeId = gradeId;
        e.cargoId = cargoId;
        e.unidadeOrganicaId = unidadeOrganicaId;
        e.dataInicio = dataInicio;
        e.dataFim = null;
        e.isCurrent = true;
        return e;
    }

    public static EnquadramentoProfissional reconstituir(EnquadramentoId id, FuncionarioId funcionarioId,
                                                          UUID careerId, UUID categoryId, UUID gradeId,
                                                          UUID cargoId, UUID unidadeOrganicaId,
                                                          LocalDate dataInicio, LocalDate dataFim,
                                                          Boolean isCurrent) {
        EnquadramentoProfissional e = new EnquadramentoProfissional();
        e.id = id;
        e.funcionarioId = funcionarioId;
        e.careerId = careerId;
        e.categoryId = categoryId;
        e.gradeId = gradeId;
        e.cargoId = cargoId;
        e.unidadeOrganicaId = unidadeOrganicaId;
        e.dataInicio = dataInicio;
        e.dataFim = dataFim;
        e.isCurrent = isCurrent;
        return e;
    }

    public void encerrar(LocalDate dataFim) {
        this.dataFim = dataFim;
        this.isCurrent = false;
    }
}
