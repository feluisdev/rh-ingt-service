package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class Funcionario {

    private FuncionarioId id;
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

    private Funcionario() {}

    public static Funcionario criar(String numeroFuncionario, String nomeCompleto, LocalDate dataNascimento,
                                    String genero, String estadoCivil, String nif, String biNumero,
                                    LocalDate biValidade, String nacionalidade, String email,
                                    String telefone, String morada, String fotoUrl,
                                    String situacaoProfissional, LocalDate dataAdmissao, LocalDate dataSaida) {
        Funcionario f = new Funcionario();
        f.id = FuncionarioId.gerarNovo();
        f.numeroFuncionario = numeroFuncionario;
        f.nomeCompleto = nomeCompleto;
        f.dataNascimento = dataNascimento;
        f.genero = genero;
        f.estadoCivil = estadoCivil;
        f.nif = nif;
        f.biNumero = biNumero;
        f.biValidade = biValidade;
        f.nacionalidade = nacionalidade != null ? nacionalidade : "CV";
        f.email = email;
        f.telefone = telefone;
        f.morada = morada;
        f.fotoUrl = fotoUrl;
        f.situacaoProfissional = situacaoProfissional;
        f.dataAdmissao = dataAdmissao;
        f.dataSaida = dataSaida;
        f.isActive = "ATIVO".equalsIgnoreCase(situacaoProfissional);
        return f;
    }

    public static Funcionario reconstituir(FuncionarioId id, String numeroFuncionario, String nomeCompleto,
                                           LocalDate dataNascimento, String genero, String estadoCivil,
                                           String nif, String biNumero, LocalDate biValidade,
                                           String nacionalidade, String email, String telefone,
                                           String morada, String fotoUrl, String situacaoProfissional,
                                           LocalDate dataAdmissao, LocalDate dataSaida, Boolean isActive) {
        Funcionario f = new Funcionario();
        f.id = id;
        f.numeroFuncionario = numeroFuncionario;
        f.nomeCompleto = nomeCompleto;
        f.dataNascimento = dataNascimento;
        f.genero = genero;
        f.estadoCivil = estadoCivil;
        f.nif = nif;
        f.biNumero = biNumero;
        f.biValidade = biValidade;
        f.nacionalidade = nacionalidade;
        f.email = email;
        f.telefone = telefone;
        f.morada = morada;
        f.fotoUrl = fotoUrl;
        f.situacaoProfissional = situacaoProfissional;
        f.dataAdmissao = dataAdmissao;
        f.dataSaida = dataSaida;
        f.isActive = isActive;
        return f;
    }

    public void atualizarSituacaoProfissional(String novaSituacao) {
        this.situacaoProfissional = novaSituacao;
    }

    public void atualizar(String nomeCompleto, LocalDate dataNascimento, String genero, String estadoCivil,
                          String nif, String biNumero, LocalDate biValidade, String nacionalidade,
                          String email, String telefone, String morada, String fotoUrl,
                          String situacaoProfissional, LocalDate dataAdmissao, LocalDate dataSaida) {
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.genero = genero;
        this.estadoCivil = estadoCivil;
        this.nif = nif;
        this.biNumero = biNumero;
        this.biValidade = biValidade;
        this.nacionalidade = nacionalidade;
        this.email = email;
        this.telefone = telefone;
        this.morada = morada;
        this.fotoUrl = fotoUrl;
        this.situacaoProfissional = situacaoProfissional;
        this.dataAdmissao = dataAdmissao;
        this.dataSaida = dataSaida;
        this.isActive = "ATIVO".equalsIgnoreCase(situacaoProfissional);
    }
}
