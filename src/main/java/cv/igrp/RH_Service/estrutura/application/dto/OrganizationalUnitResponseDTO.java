package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationalUnitResponseDTO {
    private String id;
    private String code;
    private String name;
    private String acronym;
    private String unitType;
    private String unitTypeDesc;
    private String descricao;
    private Boolean estado;
    private UUID parentUnitId;
    private Boolean isActive;
    private String estadoDesc;
    private Long nColaboradores;
}
