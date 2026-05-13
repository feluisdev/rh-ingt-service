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
                e.getDocumentTypeId(),
                e.getNumeroDocumento(),
                e.getDataEmissaoDoc(),
                e.getDataValidadeDoc(),
                e.getNacionalidade(),
                e.getEmail(),
                e.getTelefone(),
                e.getMorada(),
                e.getIlha(),
                e.getConcelho(),
                e.getLocalidade(),
                e.getWorkerStateId(),
                e.getDataAdmissao(),
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
        e.setDocumentTypeId(f.getDocumentTypeId());
        e.setNumeroDocumento(f.getNumeroDocumento());
        e.setDataEmissaoDoc(f.getDataEmissaoDoc());
        e.setDataValidadeDoc(f.getDataValidadeDoc());
        e.setNacionalidade(f.getNacionalidade());
        e.setEmail(f.getEmail());
        e.setTelefone(f.getTelefone());
        e.setMorada(f.getMorada());
        e.setIlha(f.getIlha());
        e.setConcelho(f.getConcelho());
        e.setLocalidade(f.getLocalidade());
        e.setWorkerStateId(f.getWorkerStateId());
        e.setDataAdmissao(f.getDataAdmissao());
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
        r.setDocumentTypeId(f.getDocumentTypeId() != null ? f.getDocumentTypeId().toString() : null);
        r.setNumeroDocumento(f.getNumeroDocumento());
        r.setDataEmissaoDoc(f.getDataEmissaoDoc());
        r.setDataValidadeDoc(f.getDataValidadeDoc());
        r.setNacionalidade(f.getNacionalidade());
        r.setEmail(f.getEmail());
        r.setTelefone(f.getTelefone());
        r.setMorada(f.getMorada());
        r.setIlha(f.getIlha());
        r.setConcelho(f.getConcelho());
        r.setLocalidade(f.getLocalidade());
        r.setWorkerStateId(f.getWorkerStateId() != null ? f.getWorkerStateId().toString() : null);
        r.setDataAdmissao(f.getDataAdmissao());
        r.setIsActive(f.getIsActive());
        r.setEstadoDesc(Boolean.TRUE.equals(f.getIsActive()) ? "Ativo" : "Inativo");
        return r;
    }
}
