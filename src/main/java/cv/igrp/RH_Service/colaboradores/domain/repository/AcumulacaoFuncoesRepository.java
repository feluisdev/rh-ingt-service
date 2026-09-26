package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.AcumulacaoFuncoes;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcumulacaoFuncoesId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** As acumulações de funções. */
public interface AcumulacaoFuncoesRepository {
    AcumulacaoFuncoes save(AcumulacaoFuncoes acumulacao);
    Optional<AcumulacaoFuncoes> findById(AcumulacaoFuncoesId id);
    /** Filtro opcional pelo estado; pelo início, das mais recentes. */
    List<AcumulacaoFuncoes> find(AcumulacaoFuncoes.Estado estado);
    List<AcumulacaoFuncoes> findByFuncionario(FuncionarioId funcionarioId);
    /** As autorizadas com fim até este dia (para caducar e para avisar). */
    List<AcumulacaoFuncoes> findAutorizadasComFimAte(LocalDate dia);
}
