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

    /**
     * O tipo de ausência classificado como <b>férias</b> e activo, se a instituição tiver um.
     * É por aqui que o vencimento anual sabe a que tipo se aplica — nunca pelo código.
     *
     * <p>Havendo mais do que um classificado assim (o catálogo é da instituição e nada o
     * impede), devolve o primeiro: o vencimento tem de ser determinístico, e recusar-se a
     * funcionar por causa de configuração seria pior do que escolher.
     */
    Optional<TipoAusencia> findFerias();
}
