package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.ModalidadeProvimento;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.models.Provimento;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProvimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.PeriodoProvaId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProvimentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.PeriodoProvaEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ProvimentoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsPeriodoProvaEntityRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsProvimentoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProvimentoRepositoryImpl implements ProvimentoRepository {

    private final ColabsProvimentoEntityRepository provimentos;
    private final ColabsPeriodoProvaEntityRepository periodos;
    private final JpaReferences refs;

    @Transactional
    @Override
    public Provimento save(Provimento p) {
        ProvimentoEntity e = provimentos.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new ProvimentoEntity();
            n.setId(p.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
            n.setModalidade(p.getModalidade().name());
            n.setDespachoNumero(p.getDespachoNumero());
            n.setDespachoData(p.getDespachoData());
            n.setDataPosse(p.getDataPosse());
            n.setConcursoRef(p.getConcursoRef());
            n.setVemDeOutraCarreira(p.isVemDeOutraCarreira());
            n.setAnteriorId(p.getAnteriorId() != null ? p.getAnteriorId().getValor() : null);
            n.setObservacoes(p.getObservacoes());
            return n;
        });
        e.setPeriodoProvaId(p.getPeriodoProvaId() != null ? p.getPeriodoProvaId().getValor() : null);
        return toDomain(provimentos.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Provimento> findById(ProvimentoId id) {
        return provimentos.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Provimento> findByFuncionario(FuncionarioId funcionarioId) {
        return provimentos.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public PeriodoProva save(PeriodoProva p) {
        PeriodoProvaEntity e = periodos.findById(p.getId().getValor()).orElseGet(() -> {
            var n = new PeriodoProvaEntity();
            n.setId(p.getId().getValor());
            n.setProvimento(refs.ref(ProvimentoEntity.class, p.getProvimentoId().getValor()));
            n.setFuncionario(refs.ref(FuncionarioEntity.class, p.getFuncionarioId().getValor()));
            n.setTipo(p.getTipo().name());
            n.setInicio(p.getInicio());
            n.setFimPrevisto(p.getFimPrevisto());
            n.setTutor(p.getTutorId() != null ? refs.ref(FuncionarioEntity.class, p.getTutorId().getValor()) : null);
            return n;
        });
        e.setEstado(p.getEstado().name());
        e.setDataRelatorio(p.getDataRelatorio());
        e.setAvaliacao(p.getAvaliacao() != null ? p.getAvaliacao().name() : null);
        e.setFundamentacao(p.getFundamentacao());
        e.setDataFim(p.getDataFim());
        return toDomain(periodos.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<PeriodoProva> findPeriodo(PeriodoProvaId id) {
        return periodos.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<PeriodoProva> findPeriodosDoFuncionario(FuncionarioId funcionarioId) {
        return periodos.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PeriodoProva> findEmCursoComFimEntre(LocalDate de, LocalDate ate) {
        return periodos.findEmCursoComFimEntre(de, ate).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<PeriodoProva> findEmCursoDoTutor(FuncionarioId tutorId) {
        return periodos.findEmCursoDoTutor(tutorId.getValor()).stream().map(this::toDomain).toList();
    }

    private Provimento toDomain(ProvimentoEntity e) {
        return Provimento.reconstruir(ProvimentoId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                ModalidadeProvimento.valueOf(e.getModalidade()), e.getDespachoNumero(), e.getDespachoData(), e.getDataPosse(),
                e.getConcursoRef(), Boolean.TRUE.equals(e.getVemDeOutraCarreira()),
                e.getPeriodoProvaId() != null ? PeriodoProvaId.from(e.getPeriodoProvaId()) : null,
                e.getAnteriorId() != null ? ProvimentoId.from(e.getAnteriorId()) : null, e.getObservacoes());
    }

    private PeriodoProva toDomain(PeriodoProvaEntity e) {
        var tutor = refs.idOf(e.getTutor(), FuncionarioEntity::getId);
        return PeriodoProva.reconstruir(PeriodoProvaId.from(e.getId()),
                ProvimentoId.from(refs.idOf(e.getProvimento(), ProvimentoEntity::getId)),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)), PeriodoProva.Tipo.valueOf(e.getTipo()),
                e.getInicio(), e.getFimPrevisto(), tutor != null ? FuncionarioId.from(tutor) : null,
                PeriodoProva.Estado.valueOf(e.getEstado()), e.getDataRelatorio(),
                e.getAvaliacao() != null ? PeriodoProva.Avaliacao.valueOf(e.getAvaliacao()) : null, e.getFundamentacao(), e.getDataFim());
    }
}
