package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Qualificacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.QualificacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.QualificacaoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.QualificacaoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsQualificacaoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsQualificacaoRepositoryImpl")
@RequiredArgsConstructor
public class QualificacaoRepositoryImpl implements QualificacaoRepository {

    private final ColabsQualificacaoEntityRepository entityRepository;
    private final QualificacaoMapper mapper;

    @Transactional
    @Override
    public Qualificacao save(Qualificacao qualificacao) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(qualificacao)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Qualificacao> findById(QualificacaoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Qualificacao> findAllByFuncionarioId(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionario_Id(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }
}
