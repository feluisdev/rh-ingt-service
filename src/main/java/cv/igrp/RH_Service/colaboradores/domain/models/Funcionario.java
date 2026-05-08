package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
public class Funcionario {

    private FuncionarioId id;
    private String numeroFuncionario;
    private String nomeCompleto;
    private LocalDate dataNascimento;
    private String genero;
    private String estadoCivil;
    private String nif;
    private UUID documentTypeId;
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
    private UUID workerStateId;
    private LocalDate dataAdmissao;
    private Boolean isActive;

    private Funcionario() {}

    public static Funcionario criar(String numeroFuncionario, String nomeCompleto, LocalDate dataNascimento,
                                    String genero, String estadoCivil, String nif,
                                    UUID documentTypeId, String numeroDocumento,
                                    LocalDate dataEmissaoDoc, LocalDate dataValidadeDoc,
                                    String nacionalidade, String email, String telefone,
                                    String morada, String ilha, String concelho, String localidade,
                                    UUID workerStateId, LocalDate dataAdmissao) {
        Funcionario f = new Funcionario();
        f.id = FuncionarioId.gerarNovo();
        f.numeroFuncionario = numeroFuncionario;
        f.nomeCompleto = nomeCompleto;
        f.dataNascimento = dataNascimento;
        f.genero = genero;
        f.estadoCivil = estadoCivil;
        f.nif = nif;
        f.documentTypeId = documentTypeId;
        f.numeroDocumento = numeroDocumento;
        f.dataEmissaoDoc = dataEmissaoDoc;
        f.dataValidadeDoc = dataValidadeDoc;
        f.nacionalidade = nacionalidade != null ? nacionalidade : "CV";
        f.email = email;
        f.telefone = telefone;
        f.morada = morada;
        f.ilha = ilha;
        f.concelho = concelho;
        f.localidade = localidade;
        f.workerStateId = workerStateId;
        f.dataAdmissao = dataAdmissao;
        f.isActive = true;
        return f;
    }

    public static Funcionario reconstituir(FuncionarioId id, String numeroFuncionario, String nomeCompleto,
                                           LocalDate dataNascimento, String genero, String estadoCivil,
                                           String nif, UUID documentTypeId, String numeroDocumento,
                                           LocalDate dataEmissaoDoc, LocalDate dataValidadeDoc,
                                           String nacionalidade, String email, String telefone,
                                           String morada, String ilha, String concelho, String localidade,
                                           UUID workerStateId,
                                           LocalDate dataAdmissao, Boolean isActive) {
        Funcionario f = new Funcionario();
        f.id = id;
        f.numeroFuncionario = numeroFuncionario;
        f.nomeCompleto = nomeCompleto;
        f.dataNascimento = dataNascimento;
        f.genero = genero;
        f.estadoCivil = estadoCivil;
        f.nif = nif;
        f.documentTypeId = documentTypeId;
        f.numeroDocumento = numeroDocumento;
        f.dataEmissaoDoc = dataEmissaoDoc;
        f.dataValidadeDoc = dataValidadeDoc;
        f.nacionalidade = nacionalidade;
        f.email = email;
        f.telefone = telefone;
        f.morada = morada;
        f.ilha = ilha;
        f.concelho = concelho;
        f.localidade = localidade;
        f.workerStateId = workerStateId;
        f.dataAdmissao = dataAdmissao;
        f.isActive = isActive;
        return f;
    }

    public void atualizar(String nomeCompleto, LocalDate dataNascimento, String genero, String estadoCivil,
                          String nif, UUID documentTypeId, String numeroDocumento,
                          LocalDate dataEmissaoDoc, LocalDate dataValidadeDoc,
                          String nacionalidade, String email, String telefone,
                          String morada, String ilha, String concelho, String localidade,
                          LocalDate dataAdmissao) {
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.estadoCivil = estadoCivil;
        this.nif = nif;
        this.documentTypeId = documentTypeId;
        this.numeroDocumento = numeroDocumento;
        this.dataEmissaoDoc = dataEmissaoDoc;
        this.dataValidadeDoc = dataValidadeDoc;
        this.nacionalidade = nacionalidade;
        this.email = email;
        this.telefone = telefone;
        this.morada = morada;
        this.ilha = ilha;
        this.concelho = concelho;
        this.localidade = localidade;
        this.dataAdmissao = dataAdmissao;
    }

    public void atualizarWorkerState(UUID workerStateId, boolean isActive) {
        this.workerStateId = workerStateId;
        this.isActive = isActive;
    }

}
