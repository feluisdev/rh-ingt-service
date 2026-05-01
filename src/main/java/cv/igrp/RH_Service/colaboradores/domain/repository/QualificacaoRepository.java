package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Qualificacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;

import java.util.List;
import java.util.Optional;

public interface QualificacaoRepository {
    Qualificacao save(Qualificacao qualificacao);
    Optional<Qualificacao> findById(QualificacaoId id);
    List<Qualificacao> findAllByFuncionarioId(FuncionarioId funcionarioId);
}
