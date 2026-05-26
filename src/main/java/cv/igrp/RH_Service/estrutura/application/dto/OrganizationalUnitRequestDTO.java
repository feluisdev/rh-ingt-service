package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationalUnitRequestDTO {
    private String code;
    private String name;
    private String acronym;
    private String unitType;
    private String descricao;
    private UUID parentUnitId;
}
