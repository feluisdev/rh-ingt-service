package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Formacao {

    private FormacaoId id;
    private FuncionarioId funcionarioId;
    private String name;
    private String institution;
    private String typeOptionKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationHours;
    private UUID documentId;

    private Formacao() {}

    public static Formacao criar(FuncionarioId funcionarioId, String name, String institution,
                                  String typeOptionKey, LocalDate startDate, LocalDate endDate,
                                  Integer durationHours, UUID documentId) {
        Formacao f = new Formacao();
        f.id = FormacaoId.gerarNovo();
        f.funcionarioId = funcionarioId;
        f.name = name;
        f.institution = institution;
        f.typeOptionKey = typeOptionKey;
        f.startDate = startDate;
        f.endDate = endDate;
        f.durationHours = durationHours;
        f.documentId = documentId;
        return f;
    }

    public static Formacao reconstituir(FormacaoId id, FuncionarioId funcionarioId, String name,
                                         String institution, String typeOptionKey,
                                         LocalDate startDate, LocalDate endDate,
                                         Integer durationHours, UUID documentId) {
        Formacao f = new Formacao();
        f.id = id;
        f.funcionarioId = funcionarioId;
        f.name = name;
        f.institution = institution;
        f.typeOptionKey = typeOptionKey;
        f.startDate = startDate;
        f.endDate = endDate;
        f.durationHours = durationHours;
        f.documentId = documentId;
        return f;
    }

    public void atualizar(String name, String institution, String typeOptionKey,
                          LocalDate startDate, LocalDate endDate, Integer durationHours, UUID documentId) {
        if (name != null) this.name = name;
        if (institution != null) this.institution = institution;
        if (typeOptionKey != null) this.typeOptionKey = typeOptionKey;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (durationHours != null) this.durationHours = durationHours;
        if (documentId != null) this.documentId = documentId;
    }
}
