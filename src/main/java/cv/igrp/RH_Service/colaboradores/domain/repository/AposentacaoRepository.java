package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProrrogacaoPermanencia;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProcessoAposentacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProrrogacaoPermanenciaId;

import java.util.List;
import java.util.Optional;

/** Os processos de aposentação e as prorrogações de permanência de um colaborador. */
public interface AposentacaoRepository {
    ProcessoAposentacao save(ProcessoAposentacao processo);
    Optional<ProcessoAposentacao> findProcesso(ProcessoAposentacaoId id);
    /** Do mais recente para o mais antigo. */
    List<ProcessoAposentacao> findProcessos(FuncionarioId funcionarioId);

    ProrrogacaoPermanencia save(ProrrogacaoPermanencia prorrogacao);
    Optional<ProrrogacaoPermanencia> findProrrogacao(ProrrogacaoPermanenciaId id);
    /** Da mais recente para a mais antiga. */
    List<ProrrogacaoPermanencia> findProrrogacoes(FuncionarioId funcionarioId);
}
