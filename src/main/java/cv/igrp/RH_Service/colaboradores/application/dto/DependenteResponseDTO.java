package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DependenteResponseDTO {
    private String id;
    private String funcionarioId;
    private String fullName;
    private String relationshipType;
    private LocalDate birthDate;
    private String nif;
    private Boolean isActive;
    private String estadoDesc;
}
