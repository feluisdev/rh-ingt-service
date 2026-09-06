package cv.igrp.RH_Service.sigdi.infrastructure.persistence.adapters.tatical;

import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodAuditRepository;
import cv.igrp.RH_Service.sigdi.infrastructure.persistence.entity.PaaSubmissionPeriodEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fase 133, plano 05 -- leitura pelo {@code AuditReader} do Envers, mesma forma do precedente
 * {@code colaboradores.AuditHistoryRepositoryImpl}: revisões da entidade filtradas por
 * {@code AuditEntity.id()}. Sem SQL nativo contra {@code audit_schema}: o {@code AuditReader} já
 * conhece o esquema.
 */
@Repository
@Transactional(readOnly = true)
public class PaaSubmissionPeriodAuditRepositoryImpl implements PaaSubmissionPeriodAuditRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<Revision> findRevisions(UUID id) {
        AuditReader reader = AuditReaderFactory.get(entityManager);
        List<Object[]> results = reader.createQuery()
                .forRevisionsOfEntity(PaaSubmissionPeriodEntity.class, false, true)
                .add(AuditEntity.id().eq(id))
                // Contrato da porta: ascendente, a mais antiga primeiro (ver Javadoc de
                // PaaSubmissionPeriodAuditRepository#findRevisions). Explícito em vez de confiar
                // na ordem implícita do Envers -- é a garantia que o handler e o cliente assumem.
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        List<Revision> revisions = new ArrayList<>();
        for (Object[] row : results) {
            PaaSubmissionPeriodEntity snapshot = (PaaSubmissionPeriodEntity) row[0];
            DefaultRevisionEntity revisionEntity = (DefaultRevisionEntity) row[1];
            RevisionType revisionType = (RevisionType) row[2];

            String type = switch (revisionType) {
                case ADD -> "INSERT";
                case MOD -> "UPDATE";
                case DEL -> "DELETE";
            };

            LocalDateTime revisionDate = Instant.ofEpochMilli(revisionEntity.getTimestamp())
                    .atZone(ZoneOffset.UTC)
                    .toLocalDateTime();

            revisions.add(new Revision(
                    revisionEntity.getId(),
                    type,
                    revisionDate,
                    snapshot.getStartDate(),
                    snapshot.getEndDate(),
                    snapshot.getYear(),
                    snapshot.getStatus(),
                    snapshot.getType(),
                    snapshot.getPurpose(),
                    snapshot.getCreatedDate(),
                    snapshot.getCreatedBy(),
                    snapshot.getLastModifiedBy()));
        }
        return revisions;
    }
}
