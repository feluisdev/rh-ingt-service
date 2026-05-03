package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoDisciplinar;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoDisciplinarId;

import java.util.List;
import java.util.Optional;

public interface ProcessoDisciplinarRepository {
    ProcessoDisciplinar save(ProcessoDisciplinar processo);
    Optional<ProcessoDisciplinar> findById(ProcessoDisciplinarId id);
    List<ProcessoDisciplinar> findAllByFuncionarioId(FuncionarioId funcionarioId);
}
