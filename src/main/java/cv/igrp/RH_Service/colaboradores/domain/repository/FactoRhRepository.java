package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.FactoRh;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.YearMonth;
import java.util.List;

public interface FactoRhRepository {
    FactoRh save(FactoRh facto);

    /** Os factos que entram num mês de processamento, pela ordem em que foram registados. */
    List<FactoRh> findByMesCompetencia(YearMonth mes);

    /** Os de um colaborador, do mais recente para o mais antigo. */
    List<FactoRh> findByFuncionario(FuncionarioId funcionarioId);
}
