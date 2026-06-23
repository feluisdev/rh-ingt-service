package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.HistoricoEstadoColaborador;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;

public interface HistoricoEstadoColaboradorRepository {
    HistoricoEstadoColaborador save(HistoricoEstadoColaborador historico);
    List<HistoricoEstadoColaborador> findAllByFuncionarioId(FuncionarioId funcionarioId);
}
