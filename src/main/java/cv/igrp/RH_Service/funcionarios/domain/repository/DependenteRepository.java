package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.DependenteFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Dependente;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface DependenteRepository {

  Dependente save(Dependente dependente);

  Optional<Dependente> getById(ExternalID dependenteID);

  List<Dependente> getAll();

  List<Dependente> getAll(DependenteFilter filter);

  List<Dependente> getAllByFuncionarioId(ExternalID funcionarioId);
}
