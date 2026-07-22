package cv.igrp.RH_Service.colaboradores.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioRequestDTO {

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

    private UUID documentTypeId;

    @Size(max = 50)
    private String numeroDocumento;

    private LocalDate dataEmissaoDoc;

    private LocalDate dataValidadeDoc;

    private String nacionalidade;

    @Size(max = 200)
    private String email;

    @Size(max = 30)
    private String telefone;

    private String morada;

    @Size(max = 100)
    private String ilha;

    @Size(max = 100)
    private String concelho;

    @Size(max = 100)
    private String localidade;

    private LocalDate dataAdmissao;
}
