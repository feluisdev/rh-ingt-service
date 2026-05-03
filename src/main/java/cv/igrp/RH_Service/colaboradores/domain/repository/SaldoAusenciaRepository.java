package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.SaldoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.SaldoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;

import java.util.List;
import java.util.Optional;

public interface SaldoAusenciaRepository {

    SaldoAusencia save(SaldoAusencia saldo);

    Optional<SaldoAusencia> findById(SaldoAusenciaId id);

    List<SaldoAusencia> findAllByFuncionarioId(FuncionarioId funcionarioId, SaldoAusenciaFilter filter);

    Optional<SaldoAusencia> findByFuncionarioIdAndTipoAusenciaIdAndAno(
            FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId, int ano);

    boolean existsByFuncionarioIdAndTipoAusenciaIdAndAno(
            FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId, int ano);
}
