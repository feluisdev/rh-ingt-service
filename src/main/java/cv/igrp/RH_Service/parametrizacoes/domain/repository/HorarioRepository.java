package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId;

import java.util.List;
import java.util.Optional;

public interface HorarioRepository {
    Horario save(Horario horario);
    Optional<Horario> findById(HorarioId id);
    /** Por nome; {@code active} nulo traz todos. */
    List<Horario> findAll(Boolean active);
    /** O horário da instituição, se já houver um marcado. */
    Optional<Horario> findBase();
}
