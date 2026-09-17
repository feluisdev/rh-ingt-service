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
public class PromocaoResponseDTO {
    /** Id da nova afectação criada pela promoção. */
    private String id;
    private String funcionarioId;
    private String positionId;
    private String categoriaAnteriorId;
    private String categoriaAnterior;
    private String categoriaNovaId;
    private String categoriaNova;
    private String escalaoId;
    private String escalao;
    /** true = a pessoa ficou no mesmo Lugar e o Lugar subiu de categoria. */
    private Boolean lugarReclassificado;
    private LocalDate dataEfeito;
}
