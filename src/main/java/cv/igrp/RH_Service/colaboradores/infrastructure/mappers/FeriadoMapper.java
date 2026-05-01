package cv.igrp.RH_Service.colaboradores.infrastructure.mappers;

import cv.igrp.RH_Service.colaboradores.application.dto.FeriadoResponse;
import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FeriadoEntity;
import org.springframework.stereotype.Component;

@Component("colabsFeriadoMapper")
public class FeriadoMapper {

    public Feriado toDomain(FeriadoEntity e) {
        return Feriado.reconstituir(
                FeriadoId.from(e.getId()),
                e.getName(), e.getHolidayDate(),
                e.getIsNational(), e.getDescription(),
                e.getIsActive());
    }

    public FeriadoEntity toEntity(Feriado f) {
        FeriadoEntity e = new FeriadoEntity();
        e.setId(f.getId().getValor());
        e.setName(f.getNome());
        e.setHolidayDate(f.getData());
        e.setIsNational(f.getIsNational());
        e.setDescription(f.getMunicipioCkey());
        e.setIsActive(f.getIsActive());
        return e;
    }

    public FeriadoResponse toDTO(Feriado f) {
        FeriadoResponse r = new FeriadoResponse();
        r.setId(f.getId().getStringValor());
        r.setNome(f.getNome());
        r.setData(f.getData());
        r.setIsNational(f.getIsNational());
        r.setMunicipioCkey(f.getMunicipioCkey());
        r.setIsActive(f.getIsActive());
        return r;
    }
}
