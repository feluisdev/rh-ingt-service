package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.FormacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Formacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface FormacaoRepository {
    Formacao save(Formacao formacao);
    Optional<Formacao> findById(FormacaoId id);
    List<Formacao> findAllByFuncionarioId(FuncionarioId funcionarioId, FormacaoFilter filter);
    void deleteById(FormacaoId id);
}
