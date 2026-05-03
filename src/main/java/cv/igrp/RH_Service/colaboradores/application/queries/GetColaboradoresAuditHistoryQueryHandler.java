package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AuditHistoryEntryDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaAuditHistoryDTO;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.ContratoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DependenteEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.EnquadramentoEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.FuncionarioEntity;
import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.QualificacaoEntity;
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
public class GetColaboradoresAuditHistoryQueryHandler
        implements QueryHandler<GetColaboradoresAuditHistoryQuery, ResponseEntity<WrapperListaAuditHistoryDTO>> {

    private static final Map<String, Class<?>> CATALOG_MAP = Map.of(
        "funcionarios",             FuncionarioEntity.class,
        "enquadramentos",           EnquadramentoEntity.class,
        "contratos",                ContratoEntity.class,
        "dependentes",              DependenteEntity.class,
        "qualificacoes",            QualificacaoEntity.class
    );

    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    @IgrpQueryHandler
    @SuppressWarnings("unchecked")
    public ResponseEntity<WrapperListaAuditHistoryDTO> handle(GetColaboradoresAuditHistoryQuery query) {
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
