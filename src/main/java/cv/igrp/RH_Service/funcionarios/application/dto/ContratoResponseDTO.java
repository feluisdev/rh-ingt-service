/* THIS FILE WAS GENERATED AUTOMATICALLY BY iGRP STUDIO. */
/* DO NOT MODIFY IT BECAUSE IT COULD BE REWRITTEN AT ANY TIME. */

package cv.igrp.RH_Service.funcionarios.application.dto;

import cv.igrp.framework.stereotype.IgrpDTO;
import jakarta.validation.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import cv.igrp.RH_Service.funcionarios.application.dto.DocumentoResponseDTO;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor


@IgrpDTO
public class ContratoResponseDTO {



  private String contratoId ;


  private String funcionarioId ;


  private String departamentoId ;

  private String departamento;

  private String cargoId ;

  private String cargo;

  private String tipoContrato ;


  private String tipoContratoDesc ;


  private LocalDate dataInicio ;


  private LocalDate dataFim ;


  private BigDecimal salario ;


  private Integer cargaHoraria ;


  private String observacoes ;


  private String estado ;


  private String estadoDesc ;

  @Valid
  private DocumentoResponseDTO anexo ;

}
