package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class OrganizationalUnitResponse {
    private String id;
    private String code;
    private String name;
    private String acronym;
    private UUID unitTypeOptionId;
    private UUID parentUnitId;
    private Boolean isActive;
}
