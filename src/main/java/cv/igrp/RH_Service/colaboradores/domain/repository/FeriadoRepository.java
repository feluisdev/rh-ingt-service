package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Feriado;

import java.time.LocalDate;
import java.util.List;

public interface FeriadoRepository {

    /**
     * Os feriados activos que podem cair em [{@code inicio}, {@code fim}] para quem trabalha na
     * {@code areaCkey}. Com área nula, só os que não têm área — que valem para toda a gente.
     * Os recorrentes vêm por inteiro; {@link Feriado#ocorrenciasEntre} diz onde caem.
     */
    List<Feriado> findAplicaveis(LocalDate inicio, LocalDate fim, String areaCkey);
}
