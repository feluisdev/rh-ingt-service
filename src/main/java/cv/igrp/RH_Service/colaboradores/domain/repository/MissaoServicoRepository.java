package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.MissaoServico;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.MissaoServicoId;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** As missões de serviço. */
public interface MissaoServicoRepository {
    MissaoServico save(MissaoServico missao);
    Optional<MissaoServico> findById(MissaoServicoId id);
    /** Filtros opcionais: estado e participante; das mais recentes. */
    List<MissaoServico> find(MissaoServico.Estado estado, FuncionarioId participante);
    /** As pedidas ou autorizadas do participante que se sobrepõem a {@code [de, ate]}, excepto esta. */
    List<MissaoServico> findSobrepostas(FuncionarioId participante, LocalDateTime de, LocalDateTime ate, MissaoServicoId excepto);
    /** As autorizadas ou realizadas do participante que tocam {@code [de, ate]} (para o apuramento). */
    List<MissaoServico> findQueContamEntre(FuncionarioId participante, LocalDate de, LocalDate ate);
    /** As autorizadas cujo regresso previsto foi neste dia (para lembrar o relatório). */
    List<MissaoServico> findAutorizadasComRegressoEm(LocalDate dia);
}
