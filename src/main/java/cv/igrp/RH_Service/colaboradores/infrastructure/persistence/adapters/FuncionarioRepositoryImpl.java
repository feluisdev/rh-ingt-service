package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.EnquadramentoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFuncionarioEntityRepository;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository("colabsFuncionarioRepositoryImpl")
@RequiredArgsConstructor
public class FuncionarioRepositoryImpl implements FuncionarioRepository {

    private final ColabsFuncionarioEntityRepository entityRepository;
    private final FuncionarioMapper mapper;

    @Transactional
    @Override
    public Funcionario save(Funcionario funcionario) {
        return mapper.toDomain(entityRepository.save(mapper.toEntity(funcionario)));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Funcionario> findById(FuncionarioId id) {
        return entityRepository.findById(id.getValor()).map(mapper::toDomain);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Funcionario> findAll(FuncionarioFilter filter) {
        var pageable = PageRequest.of(filter.getPage(), filter.getSize());
        return entityRepository.findAll(toSpec(filter), pageable)
                .stream().map(mapper::toDomain).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public long countAll(FuncionarioFilter filter) {
        return entityRepository.count(toSpec(filter));
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNif(String nif) {
        return entityRepository.existsByNif(nif);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNifAndIdNot(String nif, FuncionarioId id) {
        return entityRepository.existsByNifAndIdNot(nif, id.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByBiNumero(String biNumero) {
        return entityRepository.existsByBiNumero(biNumero);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByBiNumeroAndIdNot(String biNumero, FuncionarioId id) {
        return entityRepository.existsByBiNumeroAndIdNot(biNumero, id.getValor());
    }

    @Transactional(readOnly = true)
    @Override
    public List<Funcionario> findAllByIds(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return entityRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }

    private Specification<FuncionarioEntity> toSpec(FuncionarioFilter filter) {
        return (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (filter.getNome() != null && !filter.getNome().isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(cb.lower(root.get("nomeCompleto")), "%" + filter.getNome().toLowerCase() + "%"));
            }
            if (filter.getNif() != null && !filter.getNif().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("nif"), filter.getNif()));
            }
            if (filter.getSituacaoProfissional() != null && !filter.getSituacaoProfissional().isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("situacaoProfissional"), filter.getSituacaoProfissional()));
            }
            if (filter.getUnidadeOrganicaId() != null) {
                Subquery<UUID> sub = query.subquery(UUID.class);
                var epa = sub.from(EnquadramentoEntity.class);
                sub.select(epa.get("funcionarioId"))
                   .where(cb.and(
                       cb.equal(epa.get("unidadeOrganicaId"), filter.getUnidadeOrganicaId()),
                       cb.isTrue(epa.get("isCurrent"))
                   ));
                predicates = cb.and(predicates, root.get("id").in(sub));
            }
            if (filter.getCareerId() != null) {
                Subquery<UUID> sub = query.subquery(UUID.class);
                var epa = sub.from(EnquadramentoEntity.class);
                sub.select(epa.get("funcionarioId"))
                   .where(cb.and(
                       cb.equal(epa.get("careerId"), filter.getCareerId()),
                       cb.isTrue(epa.get("isCurrent"))
                   ));
                predicates = cb.and(predicates, root.get("id").in(sub));
            }

            Boolean isActive = filter.getIsActive();
            if (isActive == null) {
                predicates = cb.and(predicates, cb.isTrue(root.get("isActive")));
            } else {
                predicates = cb.and(predicates, cb.equal(root.get("isActive"), isActive));
            }

            return predicates;
        };
    }
}
