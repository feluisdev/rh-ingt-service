package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository {

  Contrato save(Contrato contrato);
  Optional<Contrato> getById(Integer id);

  Optional<Contrato> getByExternalId(ExternalID externalId);

  List<Contrato> getAll();
  List<Contrato> getAllByFuncionarioExternalId(ExternalID funcionarioExternalId);
}
