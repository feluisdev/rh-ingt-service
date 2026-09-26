package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.AcumulacaoFuncoes;
import cv.igrp.RH_Service.colaboradores.domain.repository.AcumulacaoFuncoesRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.AcumulacaoFuncoesId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AcumulacaoFuncoesEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsAcumulacaoFuncoesEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AcumulacaoFuncoesRepositoryImpl implements AcumulacaoFuncoesRepository {

    private final ColabsAcumulacaoFuncoesEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public AcumulacaoFuncoes save(AcumulacaoFuncoes a) {
        var e = entityRepository.findById(a.getId().getValor()).orElseGet(() -> {
            var n = new AcumulacaoFuncoesEntity();
            n.setId(a.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, a.getFuncionarioId().getValor()));
            return n;
        });
        e.setTipo(a.getTipo().name());
        e.setCasoPublico(a.getCasoPublico() != null ? a.getCasoPublico().name() : null);
        e.setRemunerada(a.isRemunerada());
        e.setEntidade(a.getEntidade());
        e.setFuncoes(a.getFuncoes());
        e.setHorario(a.getHorario());
        e.setHorasSemanais(a.getHorasSemanais());
        e.setInicio(a.getInicio());
        e.setFim(a.getFim());
        e.setDeclaracaoSemConflito(a.isDeclaracaoSemConflito());
        e.setEstado(a.getEstado().name());
        e.setDespacho(a.getDespacho());
        e.setDataDespacho(a.getDataDespacho());
        e.setMotivo(a.getMotivo());
        e.setDataFimEfectiva(a.getDataFimEfectiva());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<AcumulacaoFuncoes> findById(AcumulacaoFuncoesId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<AcumulacaoFuncoes> find(AcumulacaoFuncoes.Estado estado) {
        return entityRepository.find(estado != null ? estado.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AcumulacaoFuncoes> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<AcumulacaoFuncoes> findAutorizadasComFimAte(LocalDate dia) {
        return entityRepository.findAutorizadasComFimAte(dia).stream().map(this::toDomain).toList();
    }

    private AcumulacaoFuncoes toDomain(AcumulacaoFuncoesEntity e) {
        return AcumulacaoFuncoes.reconstruir(AcumulacaoFuncoesId.from(e.getId()), FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                AcumulacaoFuncoes.Tipo.valueOf(e.getTipo()), e.getCasoPublico() != null ? AcumulacaoFuncoes.CasoPublico.valueOf(e.getCasoPublico()) : null,
                Boolean.TRUE.equals(e.getRemunerada()), e.getEntidade(), e.getFuncoes(), e.getHorario(), e.getHorasSemanais(), e.getInicio(), e.getFim(),
                Boolean.TRUE.equals(e.getDeclaracaoSemConflito()), AcumulacaoFuncoes.Estado.valueOf(e.getEstado()), e.getDespacho(),
                e.getDataDespacho(), e.getMotivo(), e.getDataFimEfectiva());
    }
}
