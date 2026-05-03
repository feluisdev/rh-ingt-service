package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.FuncionarioResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import org.springframework.stereotype.Component;

@Component("colabsFuncionarioMapper")
public class FuncionarioMapper {

    public Funcionario toDomain(FuncionarioEntity e) {
        return Funcionario.reconstituir(
                FuncionarioId.from(e.getId()),
                e.getNumeroFuncionario(),
                e.getNomeCompleto(),
                e.getDataNascimento(),
                e.getGenero(),
                e.getEstadoCivil(),
                e.getNif(),
                e.getBiNumero(),
                e.getBiValidade(),
                e.getNacionalidade(),
                e.getEmail(),
                e.getTelefone(),
                e.getMorada(),
                e.getFotoUrl(),
                e.getSituacaoProfissional(),
                e.getDataAdmissao(),
                e.getDataSaida(),
                e.getIsActive()
        );
    }

    public FuncionarioEntity toEntity(Funcionario f) {
        FuncionarioEntity e = new FuncionarioEntity();
        e.setId(f.getId().getValor());
        e.setNumeroFuncionario(f.getNumeroFuncionario());
        e.setNomeCompleto(f.getNomeCompleto());
        e.setDataNascimento(f.getDataNascimento());
        e.setGenero(f.getGenero());
        e.setEstadoCivil(f.getEstadoCivil());
        e.setNif(f.getNif());
        e.setBiNumero(f.getBiNumero());
        e.setBiValidade(f.getBiValidade());
        e.setNacionalidade(f.getNacionalidade());
        e.setEmail(f.getEmail());
        e.setTelefone(f.getTelefone());
        e.setMorada(f.getMorada());
        e.setFotoUrl(f.getFotoUrl());
        e.setSituacaoProfissional(f.getSituacaoProfissional());
        e.setDataAdmissao(f.getDataAdmissao());
        e.setDataSaida(f.getDataSaida());
        e.setIsActive(f.getIsActive());
        return e;
    }

    public FuncionarioResponseDTO toDTO(Funcionario f) {
        FuncionarioResponseDTO r = new FuncionarioResponseDTO();
        r.setId(f.getId().getStringValor());
        r.setNumeroFuncionario(f.getNumeroFuncionario());
        r.setNomeCompleto(f.getNomeCompleto());
        r.setDataNascimento(f.getDataNascimento());
        r.setGenero(f.getGenero());
        r.setEstadoCivil(f.getEstadoCivil());
        r.setNif(f.getNif());
        r.setBiNumero(f.getBiNumero());
        r.setBiValidade(f.getBiValidade());
        r.setNacionalidade(f.getNacionalidade());
        r.setEmail(f.getEmail());
        r.setTelefone(f.getTelefone());
        r.setMorada(f.getMorada());
        r.setFotoUrl(f.getFotoUrl());
        r.setSituacaoProfissional(f.getSituacaoProfissional());
        r.setDataAdmissao(f.getDataAdmissao());
        r.setDataSaida(f.getDataSaida());
        r.setIsActive(f.getIsActive());
        return r;
    }
}
