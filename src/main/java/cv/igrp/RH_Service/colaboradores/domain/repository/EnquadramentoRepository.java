package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.EnquadramentoProfissional;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.EnquadramentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface EnquadramentoRepository {
    EnquadramentoProfissional save(EnquadramentoProfissional enquadramento);
    Optional<EnquadramentoProfissional> findById(EnquadramentoId id);
    Optional<EnquadramentoProfissional> findCurrentByFuncionarioId(FuncionarioId funcionarioId);
    List<EnquadramentoProfissional> findAllByFuncionarioIdOrderByDataInicioDesc(FuncionarioId funcionarioId);
}
