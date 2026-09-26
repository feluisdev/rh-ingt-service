package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.Exoneracao;
import cv.igrp.RH_Service.colaboradores.domain.repository.ExoneracaoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExoneracaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ExoneracaoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsExoneracaoEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ExoneracaoRepositoryImpl implements ExoneracaoRepository {

    private final ColabsExoneracaoEntityRepository entityRepository;
    private final JpaReferences refs;

    @Transactional
    @Override
    public Exoneracao save(Exoneracao x) {
        var e = entityRepository.findById(x.getId().getValor()).orElseGet(() -> {
            var n = new ExoneracaoEntity();
            n.setId(x.getId().getValor());
            n.setFuncionario(refs.ref(FuncionarioEntity.class, x.getFuncionarioId().getValor()));
            n.setDataPreAviso(x.getDataPreAviso());
            n.setPedidaPeloProprio(x.isPedidaPeloProprio());
            return n;
        });
        e.setDataPretendida(x.getDataPretendida());
        e.setMotivo(x.getMotivo());
        e.setEstado(x.getEstado().name());
        e.setDespacho(x.getDespacho());
        e.setDataDespacho(x.getDataDespacho());
        e.setCondicionadaAte(x.getCondicionadaAte());
        e.setDataEfeito(x.getDataEfeito());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Exoneracao> findById(ExoneracaoId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Exoneracao> find(Exoneracao.Estado estado) {
        return entityRepository.find(estado != null ? estado.name() : null).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<Exoneracao> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findDoFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    private Exoneracao toDomain(ExoneracaoEntity e) {
        return Exoneracao.reconstruir(ExoneracaoId.from(e.getId()), FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                e.getDataPreAviso(), e.getDataPretendida(), e.getMotivo(), Boolean.TRUE.equals(e.getPedidaPeloProprio()),
                Exoneracao.Estado.valueOf(e.getEstado()), e.getDespacho(), e.getDataDespacho(), e.getCondicionadaAte(), e.getDataEfeito());
    }
}
