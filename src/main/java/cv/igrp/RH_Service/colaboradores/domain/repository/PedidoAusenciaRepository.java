package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.PedidoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.PedidoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PedidoAusenciaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PedidoAusenciaRepository {
    PedidoAusencia save(PedidoAusencia pedido);
    Optional<PedidoAusencia> findById(PedidoAusenciaId id);
    List<PedidoAusencia> findAllByFuncionarioId(FuncionarioId funcionarioId, PedidoAusenciaFilter filter);
    boolean existsOverlapForFuncionario(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim);
    /** Os pedidos aprovados que tocam em [{@code dataInicio}, {@code dataFim}]. */
    List<PedidoAusencia> findAprovadosEntre(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim);
    /** Os pedidos PENDENTE destes colaboradores, do que começa mais cedo para o mais tarde. */
    List<PedidoAusencia> findPendentesDe(java.util.Collection<FuncionarioId> funcionarios);
    /** V58: há um pedido que colide com um pedido em horas nestas datas e horas. */
    boolean existsSobreposicaoEmHoras(FuncionarioId funcionarioId, LocalDate dataInicio, LocalDate dataFim,
                                      java.time.LocalTime horaInicio, java.time.LocalTime horaFim);
    /** V58: os pedidos em horas do tipo que tocam nestas datas. */
    List<PedidoAusencia> findEmHorasDoTipoEntre(FuncionarioId funcionarioId, TipoAusenciaId tipoAusenciaId,
                                                LocalDate dataInicio, LocalDate dataFim);

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
