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

    /**
     * Dias já pedidos no ano para um tipo, ignorando os rejeitados e cancelados.
     * Somado na base de dados — não se traz o histórico para contar dias.
     */
    int somarDiasNoAno(FuncionarioId funcionarioId,
                       cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId tipoAusenciaId,
                       int ano);

    /** O mesmo no mês civil — art. 15.º n.º 1 al. o) e al. q) contam por mês, não por ano. */
    int somarDiasNoMes(FuncionarioId funcionarioId,
                       cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId tipoAusenciaId,
                       int ano, int mes);
}
