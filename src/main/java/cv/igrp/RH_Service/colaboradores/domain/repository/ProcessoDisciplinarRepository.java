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

    /** Os processos com tramitação ainda em curso (lista de trabalho e prazos). */
    List<ProcessoDisciplinar> findEmCurso();

    /** Com a pena por executar — notificados, ou suspensos cuja suspensão caducou (o job decide se já é o dia). */
    List<ProcessoDisciplinar> findComPenaPorExecutar();

    /** Algum processo em que o colaborador é arguido, instaurado e por decidir (Lei n.º 20/X/2023, art. 95.º a)). */
    boolean existeArguidoEmCurso(FuncionarioId funcionarioId);

    /** Os processos do colaborador com suspensão preventiva ou pena de suspensão/inactividade (para os dias especiais). */
    List<ProcessoDisciplinar> findComAfastamento(FuncionarioId funcionarioId);

    /** Quantos processos com tramitação começaram neste ano (para o número). */
    long contarDoAno(int ano);
}
