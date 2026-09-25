package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.models.Provimento;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PeriodoProvaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProvimentoId;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Os provimentos e os períodos de prova. */
public interface ProvimentoRepository {
    Provimento save(Provimento provimento);
    Optional<Provimento> findById(ProvimentoId id);
    /** Do mais recente para o mais antigo (pela posse). */
    List<Provimento> findByFuncionario(FuncionarioId funcionarioId);

    PeriodoProva save(PeriodoProva periodo);
    Optional<PeriodoProva> findPeriodo(PeriodoProvaId id);
    List<PeriodoProva> findPeriodosDoFuncionario(FuncionarioId funcionarioId);
    /** Os em curso cujo fim previsto cai em [de, ate] — os avisos do job. */
    List<PeriodoProva> findEmCursoComFimEntre(LocalDate de, LocalDate ate);
    /** Os estágios em curso que um tutor acompanha. */
    List<PeriodoProva> findEmCursoDoTutor(FuncionarioId tutorId);
}
