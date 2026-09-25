package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.PublicacaoOficial;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PublicacaoOficialId;

import java.util.List;
import java.util.Optional;

public interface PublicacaoOficialRepository {
    PublicacaoOficial save(PublicacaoOficial publicacao);
    Optional<PublicacaoOficial> findById(PublicacaoOficialId id);
    /** Filtro opcional pelo estado; das mais antigas para as mais recentes pela data do acto. */
    List<PublicacaoOficial> find(PublicacaoOficial.Estado estado);
    List<PublicacaoOficial> findByFuncionario(FuncionarioId funcionarioId);
    /** Já há publicação (não cancelada) para este acto? Evita duplicar quando o acto se repete. */
    boolean existeParaReferencia(String referenciaTipo, String referenciaId);
}
