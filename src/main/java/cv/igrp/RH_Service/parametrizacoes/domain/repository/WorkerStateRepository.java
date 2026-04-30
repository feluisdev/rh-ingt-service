package cv.igrp.RH_Service.parametrizacoes.domain.repository;

import cv.igrp.RH_Service.parametrizacoes.domain.filter.WorkerStateFilter;
import cv.igrp.RH_Service.parametrizacoes.domain.models.WorkerState;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;

import java.util.List;
import java.util.Optional;

public interface WorkerStateRepository {
    WorkerState save(WorkerState workerState);
    Optional<WorkerState> findById(WorkerStateId id);
    boolean existsByCode(String code);
    List<WorkerState> findAll(WorkerStateFilter filter);
}
