package cv.igrp.RH_Service.estrutura.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class WrapperListaPositionDTO {
    private List<PositionResponseDTO> content;
    private long totalElements;
    private int dotacao;
    private int ocupados;
    private int vagas;
    /** Das vagas, quantas estão reservadas para quem aguarda o contrato. Contam como vagas. */
    private int reservados;
}
