package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.WorkerStateFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.shared.domain.pagination.PageResult;

import java.util.List;
import java.util.Optional;

public interface WorkerStateRepository {
    WorkerState save(WorkerState workerState);
    Optional<WorkerState> findById(WorkerStateId id);
    boolean existsByCode(String code);
    java.util.Optional<WorkerState> findByCode(String code);
    PageResult<WorkerState> findAll(WorkerStateFilter filter);

    /** Estados activos que terminam a relação de emprego público (ends_employment). */
    List<WorkerState> findAllEndingEmployment();

    /**
     * Primeiro estado activo classificado nesta situação funcional, se a instituição
     * tiver algum. É assim que se passa alguém a uma situação da lei sem escrever
     * códigos de estado no código.
     */
    Optional<WorkerState> findBySituacao(cv.igrp.RH_Service.parametrizacoes.domain.models.SituacaoFuncional situacao);
}
