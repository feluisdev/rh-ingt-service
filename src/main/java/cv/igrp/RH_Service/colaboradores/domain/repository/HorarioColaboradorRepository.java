package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.HorarioColaborador;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HorarioColaboradorRepository {
    HorarioColaborador save(HorarioColaborador horario);
    /** O histórico, do mais antigo para o mais recente. */
    List<HorarioColaborador> findByFuncionario(FuncionarioId funcionarioId);
    Optional<HorarioColaborador> findVigente(FuncionarioId funcionarioId, LocalDate data);
    /** Há uma atribuição deste horário que começou antes de {@code data}: já vigorou para alguém. */
    boolean vigorouAntesDe(cv.igrp.RH_Service.parametrizacoes.domain.valueobject.HorarioId horarioId, LocalDate data);
}
