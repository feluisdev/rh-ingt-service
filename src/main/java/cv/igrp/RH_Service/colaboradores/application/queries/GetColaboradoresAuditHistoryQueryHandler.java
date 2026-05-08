package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AuditHistoryEntryDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaAuditHistoryDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.AuditHistoryRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetColaboradoresAuditHistoryQueryHandler
        implements QueryHandler<GetColaboradoresAuditHistoryQuery, ResponseEntity<WrapperListaAuditHistoryDTO>> {

    private final AuditHistoryRepository auditHistoryRepository;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaAuditHistoryDTO> handle(GetColaboradoresAuditHistoryQuery query) {
        var revisions = auditHistoryRepository.findRevisions(
                query.getCatalog(), UUID.fromString(query.getEntityId()));

        var entries = revisions.stream()
                .map(r -> new AuditHistoryEntryDTO(r.revisionId(), r.revisionDate(), r.revisionType()))
                .toList();

        var wrapper = new WrapperListaAuditHistoryDTO();
        wrapper.setContent(entries);
        wrapper.setTotalElements((long) entries.size());

        return ResponseEntity.ok(wrapper);
    }
}
