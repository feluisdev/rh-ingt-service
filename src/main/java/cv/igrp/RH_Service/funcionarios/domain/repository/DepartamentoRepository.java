package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.DepartamentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Departamento;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface DepartamentoRepository {

  Departamento save(Departamento departamento);

  Optional<Departamento> getById(ExternalID departamantoId);

  List<Departamento> getAll();

  List<Departamento> getAll(DepartamentoFilter filter);

  List<Departamento> getAllByResponsavel(ExternalID responsavelId);

  boolean existsById(ExternalID idDepartamento);
}
