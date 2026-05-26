package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.HistoricoEstadoColaboradorId;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class HistoricoEstadoColaborador {

    private HistoricoEstadoColaboradorId id;
    private FuncionarioId funcionarioId;
    private UUID estadoAnteriorId;
    private UUID estadoNovoId;
    private String motivoCkey;
    private LocalDate dataEfectividade;
    private String observacao;
    private String registadoPor;
    private LocalDateTime registadoEm;

    private HistoricoEstadoColaborador() {}

    public static HistoricoEstadoColaborador criar(FuncionarioId funcionarioId, UUID estadoAnteriorId,
                                                    UUID estadoNovoId, String motivoCkey,
                                                    LocalDate dataEfectividade, String observacao) {
        HistoricoEstadoColaborador h = new HistoricoEstadoColaborador();
        h.id = HistoricoEstadoColaboradorId.gerarNovo();
        h.funcionarioId = funcionarioId;
        h.estadoAnteriorId = estadoAnteriorId;
        h.estadoNovoId = estadoNovoId;
        h.motivoCkey = motivoCkey;
        h.dataEfectividade = dataEfectividade;
        h.observacao = observacao;
        return h;
    }

    public static HistoricoEstadoColaborador reconstruir(HistoricoEstadoColaboradorId id, FuncionarioId funcionarioId,
                                                          UUID estadoAnteriorId, UUID estadoNovoId,
                                                          String motivoCkey, LocalDate dataEfectividade,
                                                          String observacao, String registadoPor,
                                                          LocalDateTime registadoEm) {
        HistoricoEstadoColaborador h = new HistoricoEstadoColaborador();
        h.id = id;
        h.funcionarioId = funcionarioId;
        h.estadoAnteriorId = estadoAnteriorId;
        h.estadoNovoId = estadoNovoId;
        h.motivoCkey = motivoCkey;
        h.dataEfectividade = dataEfectividade;
        h.observacao = observacao;
        h.registadoPor = registadoPor;
        h.registadoEm = registadoEm;
        return h;
    }
}
