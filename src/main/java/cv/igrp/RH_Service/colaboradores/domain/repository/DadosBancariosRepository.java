package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.DadosBancarios;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DadosBancariosId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface DadosBancariosRepository {
    DadosBancarios save(DadosBancarios dadosBancarios);
    Optional<DadosBancarios> findById(DadosBancariosId id);
    List<DadosBancarios> findAllByFuncionarioId(FuncionarioId funcionarioId);
    Optional<DadosBancarios> findActiveByFuncionarioId(FuncionarioId funcionarioId);
}
