package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.ReciboFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.ReciboVencimento;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReciboVencimentoId;

import java.util.List;
import java.util.Optional;

public interface ReciboVencimentoRepository {
    ReciboVencimento save(ReciboVencimento recibo);
    Optional<ReciboVencimento> findById(ReciboVencimentoId id);
    List<ReciboVencimento> findAllByFuncionarioId(FuncionarioId funcionarioId, ReciboFilter filter);
    boolean existsByFuncionarioIdAndPeriod(FuncionarioId funcionarioId, Integer periodMonth, Integer periodYear);
}
