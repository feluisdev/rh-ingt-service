package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.FuncionarioMapper;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository.ColabsFuncionarioEntityRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.PositionEntity;
import cv.igrp.RH_Service.shared.infrastructure.persistence.SearchSpecificationHelper;
import jakarta.persistence.criteria.Root;
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
    public boolean existsByNumeroDocumento(String numeroDocumento) {
        return entityRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Transactional(readOnly = true)
    @Override
    public boolean existsByNumeroDocumentoAndIdNot(String numeroDocumento, FuncionarioId id) {
        return entityRepository.existsByNumeroDocumentoAndIdNot(numeroDocumento, id.getValor());
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
                    SearchSpecificationHelper.nameSimilarity(cb, root.get("nomeCompleto"), filter.getNome()));
            }
            if (filter.getNif() != null && !filter.getNif().isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("nif"), filter.getNif()));
            }
            if (filter.getWorkerStateId() != null) {
                predicates = cb.and(predicates, cb.equal(root.get("workerState").get("id"), filter.getWorkerStateId()));
            }
            // Unidade e carreira derivam agora do Lugar (Position) via a Afectação corrente.
            // Subquery aninhada: funcionários com afectação corrente num Lugar que satisfaz o critério.
            if (filter.getUnidadeOrganicaId() != null) {
                Subquery<UUID> posSub = query.subquery(UUID.class);
                Root<PositionEntity> p = posSub.from(PositionEntity.class);
                posSub.select(p.get("id"))
                      .where(cb.equal(p.get("unidadeOrganica").get("id"), filter.getUnidadeOrganicaId()));

                Subquery<UUID> aSub = query.subquery(UUID.class);
                Root<AssignmentEntity> a = aSub.from(AssignmentEntity.class);
                aSub.select(a.get("funcionario").get("id"))
                    .where(cb.and(cb.isTrue(a.get("isCurrent")), a.get("position").get("id").in(posSub)));

                predicates = cb.and(predicates, root.get("id").in(aSub));
            }
            if (filter.getCareerId() != null) {
                Subquery<UUID> posSub = query.subquery(UUID.class);
                Root<PositionEntity> p = posSub.from(PositionEntity.class);
                posSub.select(p.get("id"))
                      .where(cb.equal(p.get("career").get("id"), filter.getCareerId()));

                Subquery<UUID> aSub = query.subquery(UUID.class);
                Root<AssignmentEntity> a = aSub.from(AssignmentEntity.class);
                aSub.select(a.get("funcionario").get("id"))
                    .where(cb.and(cb.isTrue(a.get("isCurrent")), a.get("position").get("id").in(posSub)));

                predicates = cb.and(predicates, root.get("id").in(aSub));
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
