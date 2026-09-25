package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Contrato;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ContratoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository {
    Contrato save(Contrato contrato);
    Optional<Contrato> findById(ContratoId id);
    Optional<Contrato> findCurrentByFuncionarioId(FuncionarioId funcionarioId);
    /** Os contratos correntes (de colaboradores activos) que terminam em [de, ate] — o aviso de fim de termo. */
    List<Contrato> findCorrentesComFimEntre(java.time.LocalDate de, java.time.LocalDate ate);
    List<Contrato> findAllByFuncionarioIdOrderByStartDateDesc(FuncionarioId funcionarioId);
    boolean existsByContractNumber(String contractNumber);
    boolean existsByContractNumberAndIdNot(String contractNumber, ContratoId id);
}
