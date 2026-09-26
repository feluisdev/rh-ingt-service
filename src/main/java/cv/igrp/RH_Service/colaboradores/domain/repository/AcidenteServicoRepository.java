package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.AcidenteServico;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcidenteServicoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

/** Os acidentes em serviço e doenças profissionais. */
public interface AcidenteServicoRepository {
    AcidenteServico save(AcidenteServico acidente);
    Optional<AcidenteServico> findById(AcidenteServicoId id);
    /** Filtro opcional pelo estado; dos mais recentes. */
    List<AcidenteServico> find(AcidenteServico.Estado estado);
    List<AcidenteServico> findByFuncionario(FuncionarioId funcionarioId);
}
