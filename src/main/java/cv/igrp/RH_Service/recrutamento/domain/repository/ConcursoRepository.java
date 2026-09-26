package cv.igrp.RH_Service.recrutamento.domain.repository;

import cv.igrp.RH_Service.recrutamento.domain.models.Candidatura;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.CandidaturaId;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Os concursos e as suas candidaturas. */
public interface ConcursoRepository {
    Concurso save(Concurso concurso);
    Optional<Concurso> findById(ConcursoId id);
    /** Filtro opcional pelo estado; dos mais recentes para os mais antigos. */
    List<Concurso> find(Concurso.Estado estado);
    boolean existeReferencia(String referencia, ConcursoId excepto);
    /** Algum concurso não terminado usa este Lugar? */
    boolean lugarEmConcursoActivo(UUID lugarId, ConcursoId excepto);

    Candidatura save(Candidatura candidatura);
    Optional<Candidatura> findCandidatura(CandidaturaId id);
    /** Pela ordem de apresentação. */
    List<Candidatura> findCandidaturas(ConcursoId concursoId);
    boolean existeCandidatura(ConcursoId concursoId, String documento, UUID funcionarioId);
    /** As candidaturas de um funcionário da casa, em todos os concursos. */
    List<Candidatura> findCandidaturasDoFuncionario(UUID funcionarioId);
}
