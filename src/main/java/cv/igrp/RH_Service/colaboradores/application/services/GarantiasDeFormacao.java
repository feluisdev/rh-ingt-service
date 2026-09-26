package cv.igrp.RH_Service.colaboradores.application.services;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.List;

/**
 * Os prazos de garantia de formação que o colaborador ainda deve (Lei n.º 20/X/2023, art. 95.º b)), numa frase cada —
 * para condicionar a exoneração voluntária. Implementa-a o módulo de formação; sem ele, não há garantias.
 */
public interface GarantiasDeFormacao {

    List<String> emCurso(FuncionarioId funcionarioId, LocalDate em);
}
