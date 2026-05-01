package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.TipoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;

import java.util.List;
import java.util.Optional;

public interface TipoAusenciaRepository {
    TipoAusencia save(TipoAusencia tipoAusencia);
    Optional<TipoAusencia> findById(TipoAusenciaId id);
    List<TipoAusencia> findAll(TipoAusenciaFilter filter);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, TipoAusenciaId id);
}
