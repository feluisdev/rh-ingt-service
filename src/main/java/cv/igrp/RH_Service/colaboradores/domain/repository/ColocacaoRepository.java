package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.ColocacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Colocacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ColocacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ColocacaoRepository {
    Colocacao save(Colocacao colocacao);
    Optional<Colocacao> findById(ColocacaoId id);
    List<Colocacao> findAllByFuncionarioId(FuncionarioId funcionarioId, ColocacaoFilter filter);
    Optional<Colocacao> findCurrentByFuncionarioId(FuncionarioId funcionarioId);
    void fecharColocacaoAtual(FuncionarioId funcionarioId, LocalDate endDate);
    boolean existsByFuncionarioId(FuncionarioId funcionarioId);

    Optional<Colocacao> findMostRecentNonMobilidadeByFuncionarioId(FuncionarioId funcionarioId);
}
