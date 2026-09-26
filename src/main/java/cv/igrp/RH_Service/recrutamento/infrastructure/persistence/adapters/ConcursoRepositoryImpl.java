package cv.igrp.RH_Service.recrutamento.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.recrutamento.domain.models.Candidatura;
import cv.igrp.RH_Service.recrutamento.domain.models.Concurso;
import cv.igrp.RH_Service.recrutamento.domain.models.MetodoSelecao;
import cv.igrp.RH_Service.recrutamento.domain.repository.ConcursoRepository;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.CandidaturaId;
import cv.igrp.RH_Service.recrutamento.domain.valueobject.ConcursoId;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.CandidaturaEntity;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.CandidaturaNotaEntity;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.ConcursoEntity;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.ConcursoJuriEntity;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.entity.ConcursoMetodoEntity;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.repository.RecrutCandidaturaEntityRepository;
import cv.igrp.RH_Service.recrutamento.infrastructure.persistence.repository.RecrutConcursoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ConcursoRepositoryImpl implements ConcursoRepository {

    private final RecrutConcursoEntityRepository concursos;
    private final RecrutCandidaturaEntityRepository candidaturas;
    private final JpaReferences refs;

    @Transactional
    @Override
    public Concurso save(Concurso c) {
        ConcursoEntity e = concursos.findById(c.getId().getValor()).orElseGet(() -> {
            var n = new ConcursoEntity();
            n.setId(c.getId().getValor());
            return n;
        });
        e.setReferencia(c.getReferencia());
        e.setFinalidade(c.getFinalidade().name());
        e.setTipo(c.getTipo().name());
        e.setModalidade(c.getModalidade().name());
        e.setVinculo(c.getVinculo());
        e.setCategoriaId(c.getCategoriaId());
        e.getLugares().clear();
        e.getLugares().addAll(c.getLugares());
        e.setRequisitos(c.getRequisitos());
        e.setHabilitacaoMinima(c.getHabilitacaoMinima());
        e.setQuotaDeficiencia(c.getQuotaDeficiencia());
        e.getMetodos().clear();
        int i = 0;
        for (var m : c.getMetodos()) {
            var x = new ConcursoMetodoEntity();
            x.setId(UUID.randomUUID());
            x.setConcurso(e);
            x.setOrdem(i++);
            x.setMetodo(m.metodo().name());
            x.setPonderacao(m.ponderacao());
            x.setEliminatorio(m.eliminatorio());
            x.setNotaMinima(m.notaMinima());
            e.getMetodos().add(x);
        }
        e.setDispensaMetodosDespacho(c.getDispensaMetodosDespacho());
        e.getJuri().clear();
        i = 0;
        for (var m : c.getJuri()) {
            var x = new ConcursoJuriEntity();
            x.setId(UUID.randomUUID());
            x.setConcurso(e);
            x.setOrdem(i++);
            x.setPapel(m.papel().name());
            x.setNome(m.nome());
            x.setFuncionarioId(m.funcionarioId());
            e.getJuri().add(x);
        }
        e.setDataAviso(c.getDataAviso());
        e.setCandidaturasDe(c.getCandidaturasDe());
        e.setCandidaturasAte(c.getCandidaturasAte());
        e.setEstado(c.getEstado().name());
        e.setHomologacaoDespacho(c.getHomologacaoDespacho());
        e.setHomologacaoData(c.getHomologacaoData());
        e.setReservaAte(c.getReservaAte());
        e.setMotivoAnulacao(c.getMotivoAnulacao());
        return toDomain(concursos.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Concurso> findById(ConcursoId id) {
        return concursos.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Concurso> find(Concurso.Estado estado) {
        return concursos.find(estado != null ? estado.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeReferencia(String referencia, ConcursoId excepto) {
        return concursos.existeReferencia(referencia, excepto != null ? excepto.getValor() : null);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean lugarEmConcursoActivo(UUID lugarId, ConcursoId excepto) {
        return concursos.lugarEmConcursoActivo(lugarId, excepto != null ? excepto.getValor() : null);
    }

    @Transactional
    @Override
    public Candidatura save(Candidatura c) {
        CandidaturaEntity e = candidaturas.findById(c.getId().getValor()).orElseGet(() -> {
            var n = new CandidaturaEntity();
            n.setId(c.getId().getValor());
            n.setConcurso(refs.ref(ConcursoEntity.class, c.getConcursoId().getValor()));
            n.setNome(c.getNome());
            n.setDocumento(c.getDocumento());
            n.setNif(c.getNif());
            n.setEmail(c.getEmail());
            n.setTelefone(c.getTelefone());
            n.setHabilitacao(c.getHabilitacao());
            n.setDeficiencia(c.isDeficiencia());
            n.setFuncionarioId(c.getFuncionarioId());
            n.setVinculadoAdministracao(c.isVinculadoAdministracao());
            n.setDataApresentacao(c.getDataApresentacao());
            return n;
        });
        e.setEstado(c.getEstado().name());
        e.setMotivoExclusao(c.getMotivoExclusao());
        e.setAudienciaAte(c.getAudienciaAte());
        e.setRespostaAudiencia(c.getRespostaAudiencia());
        e.getNotas().clear();
        for (var n : c.getNotas().entrySet()) {
            var x = new CandidaturaNotaEntity();
            x.setId(UUID.randomUUID());
            x.setCandidatura(e);
            x.setMetodo(n.getKey().name());
            x.setNota(n.getValue());
            e.getNotas().add(x);
        }
        e.setClassificacaoFinal(c.getClassificacaoFinal());
        e.setPosicao(c.getPosicao());
        e.setLugarProvidoId(c.getLugarProvidoId());
        e.setDataDesistencia(c.getDataDesistencia());
        return toDomain(candidaturas.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Candidatura> findCandidatura(CandidaturaId id) {
        return candidaturas.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Candidatura> findCandidaturas(ConcursoId concursoId) {
        return candidaturas.findDoConcurso(concursoId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existeCandidatura(ConcursoId concursoId, String documento, UUID funcionarioId) {
        return candidaturas.existe(concursoId.getValor(), documento, funcionarioId);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Candidatura> findCandidaturasDoFuncionario(UUID funcionarioId) {
        return candidaturas.findDoFuncionario(funcionarioId).stream().map(this::toDomain).toList();
    }

    private Concurso toDomain(ConcursoEntity e) {
        var metodos = e.getMetodos().stream().map(m -> new Concurso.Metodo(MetodoSelecao.valueOf(m.getMetodo()), m.getPonderacao(),
                Boolean.TRUE.equals(m.getEliminatorio()), m.getNotaMinima())).toList();
        var juri = e.getJuri().stream().map(m -> new Concurso.MembroJuri(Concurso.PapelJuri.valueOf(m.getPapel()), m.getNome(),
                m.getFuncionarioId())).toList();
        return Concurso.reconstruir(ConcursoId.from(e.getId()), e.getReferencia(), Concurso.Finalidade.valueOf(e.getFinalidade()),
                Concurso.Tipo.valueOf(e.getTipo()), Concurso.Modalidade.valueOf(e.getModalidade()), e.getVinculo(), e.getCategoriaId(),
                List.copyOf(e.getLugares()), e.getRequisitos(), e.getHabilitacaoMinima(), e.getQuotaDeficiencia(), metodos,
                e.getDispensaMetodosDespacho(), juri, e.getDataAviso(), e.getCandidaturasDe(), e.getCandidaturasAte(),
                Concurso.Estado.valueOf(e.getEstado()), e.getHomologacaoDespacho(), e.getHomologacaoData(), e.getReservaAte(),
                e.getMotivoAnulacao());
    }

    private Candidatura toDomain(CandidaturaEntity e) {
        Map<MetodoSelecao, BigDecimal> notas = new EnumMap<>(MetodoSelecao.class);
        e.getNotas().forEach(n -> notas.put(MetodoSelecao.valueOf(n.getMetodo()), n.getNota()));
        return Candidatura.reconstruir(CandidaturaId.from(e.getId()),
                ConcursoId.from(refs.idOf(e.getConcurso(), ConcursoEntity::getId)), e.getNome(), e.getDocumento(), e.getNif(),
                e.getEmail(), e.getTelefone(), e.getHabilitacao(), Boolean.TRUE.equals(e.getDeficiencia()), e.getFuncionarioId(),
                Boolean.TRUE.equals(e.getVinculadoAdministracao()), e.getDataApresentacao(), Candidatura.Estado.valueOf(e.getEstado()),
                e.getMotivoExclusao(), e.getAudienciaAte(), e.getRespostaAudiencia(), notas, e.getClassificacaoFinal(), e.getPosicao(),
                e.getLugarProvidoId(), e.getDataDesistencia());
    }
}
