package cv.igrp.RH_Service.estrutura.domain.models;

import cv.igrp.RH_Service.estrutura.domain.valueobject.OrganizationalUnitId;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.util.UUID;

@Getter
public class OrganizationalUnit {

    private OrganizationalUnitId id;
    private String code;
    private String name;
    private String acronym;
    private String unitType;
    private String descricao;
    private OrganizationalUnitId parentUnitId;
    // UUID nu e nao um value object tipado (D-09, 109-02-PLAN.md): o alvo e um
    // funcionario de outro modulo, e um identificador tipado importado desse modulo
    // levaria o alcance cross-modulo para dentro do dominio de estrutura.
    private UUID responsibleEmployeeId;
    // Onde a unidade fica, para os feriados que só valem numa área (V55): um ckey de
    // AREA_GEOGRAFICA. Nula quer dizer «a da unidade-mãe»; nula até ao topo, só contam os
    // feriados sem área.
    private String areaCkey;
    // O horário de quem trabalha na unidade e não tem um atribuído (V57). Nulo quer dizer «o da
    // unidade-mãe»; a unidade de topo faz de horário da instituição.
    private HorarioId horarioId;
    private boolean active;

    private OrganizationalUnit() {}

    private OrganizationalUnit(OrganizationalUnitId id, String code, String name, String acronym,
                                String unitType, String descricao,
                                OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId,
                                String areaCkey, boolean active) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitType = unitType;
        this.descricao = descricao;
        this.parentUnitId = parentUnitId;
        this.responsibleEmployeeId = responsibleEmployeeId;
        this.areaCkey = normalizarArea(areaCkey);
        this.active = active;
    }

    public static OrganizationalUnit criar(String code, String name, String acronym,
                                            String unitType, String descricao,
                                            OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId,
                                            String areaCkey) {
        return new OrganizationalUnit(OrganizationalUnitId.gerarNovo(), code, name, acronym,
                unitType, descricao, parentUnitId, responsibleEmployeeId, areaCkey, true);
    }

    public static OrganizationalUnit reconstruir(OrganizationalUnitId id, String code, String name, String acronym,
                                                  String unitType, String descricao,
                                                  OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId,
                                                  String areaCkey, boolean active) {
        return new OrganizationalUnit(id, code, name, acronym, unitType, descricao, parentUnitId,
                responsibleEmployeeId, areaCkey, active);
    }

    public static OrganizationalUnit reconstruir(OrganizationalUnitId id, String code, String name, String acronym,
                                                  String unitType, String descricao,
                                                  OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId,
                                                  String areaCkey, HorarioId horarioId, boolean active) {
        var unidade = reconstruir(id, code, name, acronym, unitType, descricao, parentUnitId,
                responsibleEmployeeId, areaCkey, active);
        unidade.horarioId = horarioId;
        return unidade;
    }

    public void atualizar(String code, String name, String acronym, String unitType,
                          String descricao, OrganizationalUnitId parentUnitId, UUID responsibleEmployeeId,
                          String areaCkey) {
        this.code = code;
        this.name = name;
        this.acronym = acronym;
        this.unitType = unitType;
        this.descricao = descricao;
        this.parentUnitId = parentUnitId;
        this.responsibleEmployeeId = responsibleEmployeeId;
        this.areaCkey = normalizarArea(areaCkey);
    }

    /** Nulo: a unidade segue o horário da unidade-mãe. A existência e o estado validam-se fora. */
    public void definirHorario(HorarioId horarioId) {
        this.horarioId = horarioId;
    }

    public void desativar() {
        if (!this.active) {
            throw IgrpResponseStatusException.conflict("A unidade orgânica já está inactiva.");
        }
        this.active = false;
    }

    public void reativar() {
        if (this.active) {
            throw IgrpResponseStatusException.conflict("A unidade orgânica já está activa.");
        }
        this.active = true;
    }

    private static String normalizarArea(String areaCkey) {
        return areaCkey == null || areaCkey.isBlank() ? null : areaCkey.trim();
    }
}
