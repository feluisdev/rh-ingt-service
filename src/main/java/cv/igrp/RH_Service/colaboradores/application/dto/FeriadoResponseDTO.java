package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FeriadoResponseDTO {
    private String id;
    private String nome;
    private LocalDate data;
    private Boolean isNational;
    private String municipioCkey;
    private Boolean isActive;
}
