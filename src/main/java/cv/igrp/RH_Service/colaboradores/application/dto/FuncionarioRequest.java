package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioRequest {

    @NotBlank
    @Size(max = 200)
    private String nomeCompleto;

    @NotNull
    private LocalDate dataNascimento;

    @NotBlank
    private String genero;

    @NotBlank
    private String estadoCivil;

    @NotBlank
    @Size(max = 20)
    private String nif;

    @NotBlank
    @Size(max = 50)
    private String biNumero;

    private LocalDate biValidade;

    private String nacionalidade;

    @Size(max = 200)
    private String email;

    @Size(max = 30)
    private String telefone;

    private String morada;

    private String fotoUrl;

    @NotBlank
    private String situacaoProfissional;

    @NotNull
    private LocalDate dataAdmissao;

    private LocalDate dataSaida;
}
