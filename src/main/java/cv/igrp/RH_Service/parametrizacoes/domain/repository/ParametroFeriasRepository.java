package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.models.ParametroFerias;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.ParametroFeriasId;

import java.util.List;
import java.util.Optional;

public interface ParametroFeriasRepository {
    ParametroFerias save(ParametroFerias parametro);
    Optional<ParametroFerias> findById(ParametroFeriasId id);
    /** A linha que vale em {@code ano}: a de maior início de vigência que não passe dele. */
    Optional<ParametroFerias> findVigenteEm(int ano);
    /** Todas as vigências, da mais antiga para a mais recente. */
    List<ParametroFerias> findAll();
    boolean existsByVigenteDesde(int vigenteDesde);
}
