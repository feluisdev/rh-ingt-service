package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.CargoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Cargo;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface CargoRepository {

  Cargo save(Cargo cargo);

  Optional<Cargo> getByExternalId(ExternalID externalId);

  List<Cargo> getAll();

  List<Cargo> getAll(CargoFilter filter);
}
