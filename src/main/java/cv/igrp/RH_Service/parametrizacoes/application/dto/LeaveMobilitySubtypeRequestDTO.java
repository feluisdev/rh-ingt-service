package cv.igrp.RH_Service.parametrizacoes.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@IgrpDTO
public class LeaveMobilitySubtypeRequestDTO {

    @NotBlank
    private String code;

    private String description;

    @NotBlank
    private String recordType;

    private boolean affectsPay;

    private boolean countsForSeniority;

    private boolean canSelfSubmit;

    /** Duração máxima em dias (mobilidade transitória: 365 — art. 132.º n.º 5). Nulo = sem limite. */
    private Integer maxDurationDays;

    /** Prorrogações permitidas (em regra, uma). Nulo = sem limite. */
    private Integer maxExtensions;

    /** MANTEM | ABRE_VAGA — o que a licença faz ao Lugar (DL n.º 3/2010). */
    private String positionEffect;

    /** Abre vaga só além deste número de dias; nulo = abre logo. */
    private Integer vacancyAfterDays;

    /** REGRESSA_LUGAR | DISPONIBILIDADE — o que acontece no regresso (art. 122.º). */
    private String returnEffect;
}
