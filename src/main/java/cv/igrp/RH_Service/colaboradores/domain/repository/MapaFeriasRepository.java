package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.MapaFerias;

import java.util.Optional;

public interface MapaFeriasRepository {

    MapaFerias save(MapaFerias mapa);

    Optional<MapaFerias> findByAno(int ano);
}
