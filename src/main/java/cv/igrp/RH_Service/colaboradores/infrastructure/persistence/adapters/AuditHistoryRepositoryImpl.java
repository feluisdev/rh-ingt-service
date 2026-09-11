package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.adapters;

import cv.igrp.RH_Service.colaboradores.domain.models.AuditRevision;
import cv.igrp.RH_Service.colaboradores.domain.repository.AuditHistoryRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.AssignmentEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DependenteEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.QualificacaoEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
public class AuditHistoryRepositoryImpl implements AuditHistoryRepository {

    private static final Map<String, Class<?>> CATALOG_MAP = Map.of(
        "funcionarios",   FuncionarioEntity.class,
        "assignments",    AssignmentEntity.class,
        "contratos",      ContratoEntity.class,
        "dependentes",    DependenteEntity.class,
        "qualificacoes",  QualificacaoEntity.class
    );

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AuditRevision> findRevisions(String catalog, UUID entityId) {
        Class<?> entityClass = CATALOG_MAP.get(catalog);
        if (entityClass == null)
            throw IgrpResponseStatusException.badRequest(
                "Catálogo desconhecido: '" + catalog + "'. Valores aceites: " + CATALOG_MAP.keySet());

        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Object[]> results = reader.createQuery()
            .forRevisionsOfEntity(entityClass, false, true)
            .add(AuditEntity.id().eq(entityId))
            .getResultList();

        List<AuditRevision> entries = new ArrayList<>();
        for (Object[] row : results) {
            var revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) row[1];
            var revisionType = (RevisionType) row[2];

            String type = switch (revisionType) {
                case ADD -> "INSERT";
                case MOD -> "UPDATE";
                case DEL -> "DELETE";
            };

            entries.add(new AuditRevision(
                revisionEntity.getId(),
                FORMATTER.format(Instant.ofEpochMilli(revisionEntity.getTimestamp())),
                type));
        }
        return entries;
    }
}
