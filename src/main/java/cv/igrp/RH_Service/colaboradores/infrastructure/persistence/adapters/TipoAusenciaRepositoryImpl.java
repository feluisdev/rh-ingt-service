package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.TipoAusenciaFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.TipoAusencia;
import cv.igrp.RH_Service.colaboradores.domain.repository.TipoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.TipoAusenciaId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.entity.LeaveTypeEntity;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.persistence.repository.LeaveTypeEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository("colabsTipoAusenciaRepositoryImpl")
@RequiredArgsConstructor
public class TipoAusenciaRepositoryImpl implements TipoAusenciaRepository {

    private final LeaveTypeEntityRepository entityRepository;

    private TipoAusencia toDomain(LeaveTypeEntity e) {
        return TipoAusencia.reconstituir(
                TipoAusenciaId.from(e.getId()),
                e.getDescription(), e.getCode(),
                e.getDeductsBalance(), e.getRequiresApproval(),
                e.getMaxDaysPerYear(),
                e.getCategory(),
                e.getIsActive());
    }

    private LeaveTypeEntity toEntity(TipoAusencia t) {
        LeaveTypeEntity e = new LeaveTypeEntity();
        e.setId(t.getId().getValor());
        e.setDescription(t.getNome());
        e.setCode(t.getCodigo());
        e.setDeductsBalance(t.getDeductsBalance());
        e.setRequiresApproval(t.getRequiresApproval());
        e.setMaxDaysPerYear(t.getMaxDaysPerYear());
        e.setCategory(t.getCategoryOptionCkey());
        e.setIsActive(t.getIsActive());
        return e;
    }

    @Transactional
    @Override
    public TipoAusencia save(TipoAusencia tipoAusencia) {
        return toDomain(entityRepository.save(toEntity(tipoAusencia)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<TipoAusencia> findById(TipoAusenciaId id) {
        return entityRepository.findById(id.getValor()).map(this::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TipoAusencia> findAll(TipoAusenciaFilter filter) {
        if (filter.getActive() != null)
            return entityRepository.findAllByIsActive(filter.getActive()).stream().map(this::toDomain).toList();
        return entityRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigo(String codigo) {
        return entityRepository.existsByCode(codigo);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByCodigoAndIdNot(String codigo, TipoAusenciaId id) {
        return entityRepository.existsByCodeAndIdNot(codigo, id.getValor());
    }
}
