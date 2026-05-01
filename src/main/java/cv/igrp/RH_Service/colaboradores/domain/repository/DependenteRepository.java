package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Dependente;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DependenteId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface DependenteRepository {
    Dependente save(Dependente dependente);
    Optional<Dependente> findById(DependenteId id);
    List<Dependente> findAllByFuncionarioId(FuncionarioId funcionarioId);
}
