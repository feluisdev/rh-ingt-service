package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.AuditHistoryEntryDTO;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaAuditHistoryDTO;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.FunctionEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.JobEntity;
import cv.igrp.RH_Service.estrutura.infrastructure.persistence.entity.OrganizationalUnitEntity;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetEstruturaAuditHistoryQueryHandler
        implements QueryHandler<GetEstruturaAuditHistoryQuery, ResponseEntity<WrapperListaAuditHistoryDTO>> {

    private static final Map<String, Class<?>> CATALOG_MAP = Map.of(
        "organizational-units", OrganizationalUnitEntity.class,
        "jobs",                 JobEntity.class,
        "functions",            FunctionEntity.class
    );

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @IgrpQueryHandler
    @SuppressWarnings("unchecked")
    public ResponseEntity<WrapperListaAuditHistoryDTO> handle(GetEstruturaAuditHistoryQuery query) {
        Class<?> entityClass = CATALOG_MAP.get(query.getCatalog());
        if (entityClass == null) {
            throw IgrpResponseStatusException.badRequest(
                "Catálogo desconhecido: '" + query.getCatalog() + "'. Valores aceites: " + CATALOG_MAP.keySet());
        }

        UUID entityId = UUID.fromString(query.getEntityId());

        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Object[]> results = reader.createQuery()
            .forRevisionsOfEntity(entityClass, false, true)
            .add(AuditEntity.id().eq(entityId))
            .getResultList();

        List<AuditHistoryEntryDTO> entries = new ArrayList<>();
        for (Object[] row : results) {
            var revisionEntity = (org.hibernate.envers.DefaultRevisionEntity) row[1];
            var revisionType = (RevisionType) row[2];

            String type = switch (revisionType) {
                case ADD -> "INSERT";
                case MOD -> "UPDATE";
                case DEL -> "DELETE";
            };

            String revisionDate = FORMATTER.format(
                Instant.ofEpochMilli(revisionEntity.getTimestamp()));

            entries.add(new AuditHistoryEntryDTO(revisionEntity.getId(), revisionDate, type));
        }

        var wrapper = new WrapperListaAuditHistoryDTO();
        wrapper.setContent(entries);
        wrapper.setTotalElements((long) entries.size());

        return ResponseEntity.ok(wrapper);
    }
}
