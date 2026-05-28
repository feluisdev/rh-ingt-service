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
    private String documentTypeId;
    private String numeroDocumento;
    private LocalDate dataEmissaoDoc;
    private LocalDate dataValidadeDoc;
    private String nacionalidade;
    private String email;
    private String telefone;
    private String morada;
    private String ilha;
    private String concelho;
    private String localidade;
    private String workerStateId;
    private String workerStateName;
    private LocalDate dataAdmissao;
    private Boolean isActive;
    private String estadoDesc;
}
