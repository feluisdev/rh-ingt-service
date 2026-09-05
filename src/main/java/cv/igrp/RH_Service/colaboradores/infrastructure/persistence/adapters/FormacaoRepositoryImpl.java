package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.FormacaoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Formacao;
import cv.igrp.RH_Service.colaboradores.domain.repository.FormacaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FormacaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FormacaoMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFormacaoEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsFormacaoRepositoryImpl")
@RequiredArgsConstructor
public class FormacaoRepositoryImpl implements FormacaoRepository {

    private final ColabsFormacaoEntityRepository entityRepository;
    private final FormacaoMapper mapper;

    @Transactional
    @Override
    public Formacao save(Formacao formacao) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(formacao)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Formacao> findById(FormacaoId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Formacao> findAllByFuncionarioId(FuncionarioId funcionarioId, FormacaoFilter filter) {
        if (filter != null && filter.getYear() != null) {
            return entityRepository.findAllByFuncionarioIdAndYear(funcionarioId.getValor(), filter.getYear())
                    .stream().map(mapper::toDomain).toList();
        }
        return entityRepository.findAllByFuncionario_Id(funcionarioId.getValor())
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional
    @Override
    public void deleteById(FormacaoId id) {
        entityRepository.deleteById(id.getValor());
    }
}
