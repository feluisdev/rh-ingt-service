package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsolidacaoMobilidadeResponseDTO {
    /** Id da nova afectação, no Lugar de destino. */
    private String id;
    private String funcionarioId;
    /** A mobilidade que se consolidou. Continua deferida; o que acabou foi o período transitório. */
    private String mobilidadeId;
    /** Último dia em mobilidade — a véspera da data de efeito. */
    private LocalDate mobilidadeDataFim;
    private String positionAnteriorId;
    private String numeroLugarAnterior;
    private String unidadeOrganicaAnteriorId;
    private String positionId;
    private String numeroLugar;
    private String unidadeOrganicaId;
    private LocalDate dataEfeito;
}
