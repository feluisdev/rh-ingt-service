package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.ExameSaude;
import cv.igrp.RH_Service.colaboradores.domain.models.JuntaMedica;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaudeTrabalhoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExameSaudeId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.JuntaMedicaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ExameSaudeEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.JuntaMedicaEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsExameSaudeEntityRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsJuntaMedicaEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SaudeTrabalhoRepositoryImpl implements SaudeTrabalhoRepository {

    private final ColabsExameSaudeEntityRepository exameRepository;
    private final ColabsJuntaMedicaEntityRepository juntaRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public ExameSaude save(ExameSaude x) {
        var e = exameRepository.findById(x.getId().getValor()).orElseGet(() -> {
            var n = new ExameSaudeEntity();
            n.setId(x.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, x.getFuncionarioId().getValor()));
            return n;
        });
        e.setTipo(x.getTipo().name());
        e.setData(x.getData());
        e.setEntidade(x.getEntidade());
        e.setResultado(x.getResultado().name());
        e.setRestricoes(x.getRestricoes());
        e.setValidadeAte(x.getValidadeAte());
        e.setObservacoes(x.getObservacoes());
        return toDomain(exameRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<ExameSaude> findExame(ExameSaudeId id) {
        return exameRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExameSaude> findExames(FuncionarioId funcionarioId) {
        return exameRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExameSaude> findUltimosComValidadeEm(LocalDate dia) {
        return exameRepository.findUltimosComValidadeEm(dia).stream().map(this::toDomain).toList();
    }

    @Transactional
    @Override
    public JuntaMedica save(JuntaMedica j) {
        var e = juntaRepository.findById(j.getId().getValor()).orElseGet(() -> {
            var n = new JuntaMedicaEntity();
            n.setId(j.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, j.getFuncionarioId().getValor()));
            return n;
        });
        e.setMotivo(j.getMotivo().name());
        e.setFundamentacao(j.getFundamentacao());
        e.setDataPedido(j.getDataPedido());
        e.setDataJunta(j.getDataJunta());
        e.setParecer(j.getParecer() != null ? j.getParecer().name() : null);
        e.setDiasIncapacidade(j.getDiasIncapacidade());
        e.setObservacoes(j.getObservacoes());
        e.setEstado(j.getEstado().name());
        return toDomain(juntaRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<JuntaMedica> findJunta(JuntaMedicaId id) {
        return juntaRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<JuntaMedica> findJuntas(JuntaMedica.Estado estado) {
        return juntaRepository.find(estado != null ? estado.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<JuntaMedica> findJuntas(FuncionarioId funcionarioId) {
        return juntaRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    private ExameSaude toDomain(ExameSaudeEntity e) {
        return ExameSaude.reconstruir(ExameSaudeId.from(e.getId()), FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                ExameSaude.Tipo.valueOf(e.getTipo()), e.getData(), e.getEntidade(), ExameSaude.Resultado.valueOf(e.getResultado()), e.getRestricoes(),
                e.getValidadeAte(), e.getObservacoes());
    }

    private JuntaMedica toDomain(JuntaMedicaEntity e) {
        return JuntaMedica.reconstruir(JuntaMedicaId.from(e.getId()), FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                JuntaMedica.Motivo.valueOf(e.getMotivo()), e.getFundamentacao(), e.getDataPedido(), e.getDataJunta(),
                e.getParecer() != null ? JuntaMedica.Parecer.valueOf(e.getParecer()) : null, e.getDiasIncapacidade(), e.getObservacoes(),
                JuntaMedica.Estado.valueOf(e.getEstado()));
    }
}
