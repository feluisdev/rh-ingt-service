package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.FactoRh;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoFactoRh;
import cv.igrp.RH_Service.colaboradores.domain.repository.FactoRhRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FactoRhId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FactoRhEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFactoRhEntityRepository;
import cv.igrp.RH_Service.shared.infrastructure.persistence.JpaReferences;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class FactoRhRepositoryImpl implements FactoRhRepository {

    private final ColabsFactoRhEntityRepository entityRepository;
    private final JpaReferences refs;

    /** Só insere: um facto não muda depois de registado. */
    @Transactional
    @Override
    public FactoRh save(FactoRh f) {
        if (entityRepository.existsById(f.getId().getValor())) return f;
        FactoRhEntity e = new FactoRhEntity();
        e.setId(f.getId().getValor());
        e.setFuncionario(refs.ref(FuncionarioEntity.class, f.getFuncionarioId().getValor()));
        e.setTipo(f.getTipo().name());
        e.setDataEfeito(f.getDataEfeito());
        e.setMesCompetencia(f.getMesCompetencia().toString());
        e.setReferenciaTipo(f.getReferenciaTipo());
        e.setReferenciaId(f.getReferenciaId());
        e.setDescricao(f.getDescricao() != null && f.getDescricao().length() > 500
                ? f.getDescricao().substring(0, 500) : f.getDescricao());
        e.setDados(new LinkedHashMap<>(f.getDados()));
        e.setRegistadoEm(f.getRegistadoEm());
        return toDomain(entityRepository.save(e));
    }

    @Transactional(readOnly = true)
    @Override
    public List<FactoRh> findByMesCompetencia(YearMonth mes) {
        return entityRepository.findByMes(mes.toString()).stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public List<FactoRh> findByFuncionario(FuncionarioId funcionarioId) {
        return entityRepository.findByFuncionario(funcionarioId.getValor()).stream().map(this::toDomain).toList();
    }

    private FactoRh toDomain(FactoRhEntity e) {
        return FactoRh.reconstruir(FactoRhId.from(e.getId()),
                FuncionarioId.from(refs.idOf(e.getFuncionario(), FuncionarioEntity::getId)),
                TipoFactoRh.valueOf(e.getTipo()), e.getDataEfeito(), YearMonth.parse(e.getMesCompetencia()),
                e.getReferenciaTipo(), e.getReferenciaId(), e.getDescricao(), e.getDados(), e.getRegistadoEm());
    }
}
