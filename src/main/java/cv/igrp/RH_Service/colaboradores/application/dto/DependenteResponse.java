package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class DependenteResponse {
    private String id;
    private String funcionarioId;
    private String nome;
    private String parentesco;
    private LocalDate dataNascimento;
    private String nif;
    private Boolean isActive;
}
