package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FeriadoRequest {
    @NotBlank
    private String nome;
    @NotNull
    private LocalDate data;
    @NotNull
    private Boolean isNational;
    private String municipioCkey;
}
