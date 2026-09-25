package cv.igrp.RH_Service.sigdi.application.queries;

import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.sigdi.application.constants.Purpose;
import cv.igrp.RH_Service.sigdi.application.dto.PaaSubmissionPeriodRevisionDTO;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodAuditRepository;
import cv.igrp.RH_Service.sigdi.domain.tatical.repository.PaaSubmissionPeriodRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Fase 133, plano 05 ({@code JAN-03}, metade de leitura): {@code GET periods/{id}/audit}.
 *
 * <p><strong>404 vs. lista vazia -- discriminado, não presumido (CLAUDE.md regra 5).</strong> Uma
 * janela que não existe em {@code t_paa_submission_period} responde {@code 404}; uma janela que
 * existe mas ainda não tem revisões responde {@code 200} com lista vazia. As duas causas de
 * "nada para mostrar" são distintas e não podem colapsar na mesma resposta.
 */
@Component
public class GetPaaSubmissionPeriodAuditQueryHandler
        implements QueryHandler<GetPaaSubmissionPeriodAuditQuery, ResponseEntity<List<PaaSubmissionPeriodRevisionDTO>>> {

    private final PaaSubmissionPeriodRepository periodRepository;
    private final PaaSubmissionPeriodAuditRepository auditRepository;

    public GetPaaSubmissionPeriodAuditQueryHandler(PaaSubmissionPeriodRepository periodRepository,
                                                     PaaSubmissionPeriodAuditRepository auditRepository) {
        this.periodRepository = periodRepository;
        this.auditRepository = auditRepository;
    }

    @IgrpQueryHandler
    @Override
    public ResponseEntity<List<PaaSubmissionPeriodRevisionDTO>> handle(GetPaaSubmissionPeriodAuditQuery query) {
        periodRepository.findById(query.getId())
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Período de submissão não encontrado"));

        List<PaaSubmissionPeriodAuditRepository.Revision> revisions = auditRepository.findRevisions(query.getId());

        List<PaaSubmissionPeriodRevisionDTO> response = revisions.stream()
                .map(GetPaaSubmissionPeriodAuditQueryHandler::toDTO)
                .toList();

        return ResponseEntity.ok(response);
    }

    private static PaaSubmissionPeriodRevisionDTO toDTO(PaaSubmissionPeriodAuditRepository.Revision revision) {
        PaaSubmissionPeriodRevisionDTO dto = new PaaSubmissionPeriodRevisionDTO();
        dto.setRevision(revision.revisionNumber());
        dto.setRevisionType(revision.revisionType());
        dto.setRevisionDate(revision.revisionDate());
        dto.setModifiedBy(resolveModifiedBy(revision));
        dto.setStartDate(revision.startDate());
        dto.setEndDate(revision.endDate());
        dto.setYear(revision.year());
        dto.setStatus(revision.status());
        dto.setType(revision.type());
        dto.setPurpose(revision.purpose());
        // fromCode, nunca fromCodeOrThrow: um código histórico que o enum atual não reconheça não
        // pode transformar uma leitura de auditoria num erro (mesmo padrão de
        // resolveSkipReasonDescription em GetPeriodGenerationQueryHandler).
        dto.setPurposeDesc(Purpose.fromCode(revision.purpose())
                .map(Purpose::getDescription)
                .orElse(revision.purpose()));
        return dto;
    }

    /**
     * Decisão de payload (133-05): na revisão de criação (INSERT) usa-se {@code createdBy}; nas
     * restantes usa-se {@code lastModifiedBy}, com {@code createdBy} como recurso se aquele vier
     * nulo. Não é coincidência -- é a única forma de nunca devolver este campo vazio, dado que
     * {@code lastModifiedBy} só é preenchido a partir da primeira alteração.
     */
    private static String resolveModifiedBy(PaaSubmissionPeriodAuditRepository.Revision revision) {
        if ("INSERT".equals(revision.revisionType())) {
            return revision.createdBy();
        }
        return revision.lastModifiedBy() != null ? revision.lastModifiedBy() : revision.createdBy();
    }
}
