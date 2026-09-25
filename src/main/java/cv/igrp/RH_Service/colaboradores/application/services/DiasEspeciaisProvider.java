package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.models.EstadoDiaApurado;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.Map;

/**
 * Uma razão, vinda de outro processo, para um dia <b>não se apurar</b> contra o horário (BR-FAL-09): a
 * missão de serviço, a formação, a suspensão disciplinar, a incapacidade por acidente em serviço. Cada
 * frente implementa-a num {@code @Component}; o {@link ApuramentoFaltasService} consulta todas.
 *
 * <p>Devolve os dias de {@code [de, ate]} cobertos, com o estado que o apuramento lhes dá. Um dia que
 * nenhuma cobre segue o apuramento normal.
 */
public interface DiasEspeciaisProvider {

    Map<LocalDate, EstadoDiaApurado> dias(FuncionarioId funcionarioId, LocalDate de, LocalDate ate);
}
