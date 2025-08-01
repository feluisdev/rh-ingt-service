package cv.igrp.RH_Service.funcionarios.domain.repository;

import cv.igrp.RH_Service.funcionarios.domain.filter.QualificacaoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Qualificacao;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface QualificacaoRepository {

  Qualificacao save(Qualificacao qualificacao);

  Optional<Qualificacao> getByExternalId(ExternalID externalId);

  List<Qualificacao> getAll();

  List<Qualificacao> getAll(QualificacaoFilter filter); // todo later if i want extra filtering with speciffications

  List<Qualificacao> getAllByFuncionarioExternalId(ExternalID funcionarioExternalId);
}
