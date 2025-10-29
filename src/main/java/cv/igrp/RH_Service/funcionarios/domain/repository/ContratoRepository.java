package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface ContratoRepository {

  Contrato save(Contrato contrato);

  Optional<Contrato> getById(ExternalID contratoId);

  List<Contrato> getAll();
  List<Contrato> getAllByFuncionariolId(ExternalID funcionarioId);
}
