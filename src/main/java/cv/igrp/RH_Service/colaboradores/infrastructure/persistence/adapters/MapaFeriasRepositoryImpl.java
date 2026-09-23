package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.MapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.repository.MapaFeriasRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MapaFeriasId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.MapaFeriasEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsMapaFeriasEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MapaFeriasRepositoryImpl implements MapaFeriasRepository {

    private final ColabsMapaFeriasEntityRepository entityRepository;

    @Transactional
    @Override
    public MapaFerias save(MapaFerias m) {
        MapaFeriasEntity e = new MapaFeriasEntity();
        e.setId(m.getId().getValor());
        e.setAno(m.getAno());
        e.setPublicadoEm(m.getPublicadoEm());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<MapaFerias> findByAno(int ano) {
        return entityRepository.findByAno(ano).map(this::toDomain);
    }

    private MapaFerias toDomain(MapaFeriasEntity e) {
        return MapaFerias.reconstituir(MapaFeriasId.from(e.getId()), e.getAno(), e.getPublicadoEm());
    }
}
