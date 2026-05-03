package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository {
    Contrato save(Contrato contrato);
    Optional<Contrato> findById(ContratoId id);
    List<Contrato> findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId funcionarioId);
    boolean existsActiveByFuncionarioId(FuncionarioId funcionarioId);
    long countActiveByFuncionarioId(FuncionarioId funcionarioId);
    boolean existsByNumeroContrato(String numeroContrato);
    boolean existsByNumeroContratoAndIdNot(String numeroContrato, ContratoId id);
}
