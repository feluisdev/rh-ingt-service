package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Funcionario;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface FuncionarioRepository {

  Funcionario save(Funcionario funcionario);

  Optional<Funcionario> getById(ExternalID idFuncionario);

  List<Funcionario> getAll(FuncionarioFilter filter);

  List<Funcionario> getAll();

  boolean existsById(ExternalID idFuncionario);
}
