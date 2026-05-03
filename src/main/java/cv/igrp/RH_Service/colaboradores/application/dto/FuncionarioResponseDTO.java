package cv.igrp.RH_Service.colaboradores.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class FuncionarioResponseDTO {
    private String id;
    private String numeroFuncionario;
    private String nomeCompleto;
    private LocalDate dataNascimento;
    private String genero;
    private String estadoCivil;
    private String nif;
    private String biNumero;
    private LocalDate biValidade;
    private String nacionalidade;
    private String email;
    private String telefone;
    private String morada;
    private String fotoUrl;
    private String situacaoProfissional;
    private LocalDate dataAdmissao;
    private LocalDate dataSaida;
    private Boolean isActive;
}
