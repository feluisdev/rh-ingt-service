package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoAusenciaRepository {
    PedidoAusencia save(PedidoAusencia pedido);
    Optional<PedidoAusencia> findById(PedidoAusenciaId id);
    List<PedidoAusencia> findAllByFuncionarioId(FuncionarioId funcionarioId, PedidoAusenciaFilter filter);
    boolean existsOverlapForFuncionario(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim);
}
