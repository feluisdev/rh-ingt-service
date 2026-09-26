package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.Exoneracao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExoneracaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

/** As exonerações voluntárias. */
public interface ExoneracaoRepository {
    Exoneracao save(Exoneracao exoneracao);
    Optional<Exoneracao> findById(ExoneracaoId id);
    /** Filtro opcional pelo estado; das mais recentes. */
    List<Exoneracao> find(Exoneracao.Estado estado);
    List<Exoneracao> findByFuncionario(FuncionarioId funcionarioId);
}
