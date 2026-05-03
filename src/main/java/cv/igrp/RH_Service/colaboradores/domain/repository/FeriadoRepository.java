package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.FeriadoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FeriadoId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeriadoRepository {
    Feriado save(Feriado feriado);
    Optional<Feriado> findById(FeriadoId id);
    List<Feriado> findAll(FeriadoFilter filter);
    boolean existsNacionalActivoByData(LocalDate data);
    List<LocalDate> findAllNacionaisActivosByAno(int ano);
}
