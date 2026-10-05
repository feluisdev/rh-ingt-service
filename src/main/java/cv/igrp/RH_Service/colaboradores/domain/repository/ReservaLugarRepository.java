package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** As reservas de Lugar (BR-AF-23 a BR-AF-28). Há no máximo uma activa por Lugar e uma por colaborador. */
public interface ReservaLugarRepository {
    ReservaLugar save(ReservaLugar reserva);
    Optional<ReservaLugar> findActivaByFuncionario(FuncionarioId funcionarioId);
    Optional<ReservaLugar> findActivaByPosition(UUID positionId);
    /** As reservas activas destes Lugares, numa consulta só. */
    List<ReservaLugar> findActivasByPositions(Collection<UUID> positionIds);
    /** As reservas activas feitas num destes dias — para o aviso das reservas paradas. */
    List<ReservaLugar> findActivasReservadasEm(Collection<LocalDate> dias);
}
