package cv.igrp.RH_Service.sigdi.domain.tatical.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Fase 133, plano 05 ({@code JAN-03}, metade de leitura): porta de leitura sobre
 * {@code audit_schema.t_paa_submission_period_aud} (Hibernate Envers). Uma só entidade, um só
 * método -- ao contrário do catálogo genérico de {@code colaboradores.AuditHistoryRepository},
 * aqui não há um segundo tipo auditado a justificar um mapa de catálogo.
 */
public interface PaaSubmissionPeriodAuditRepository {

    /**
     * Devolve as revisões da janela identificada por {@code id}, <strong>ordenadas por número de
     * revisão ascendente</strong> (a mais antiga primeiro). Decisão de 133-05: a diferença entre
     * revisões consecutivas lê-se naturalmente de anterior para seguinte; a inversão para
     * apresentação (mais recente primeiro) é trabalho do cliente, não desta porta. Uma janela sem
     * nenhuma revisão devolve lista vazia -- não é erro desta porta decidir isso, é o handler que
     * decide entre 404 (janela não existe) e lista vazia (janela existe, sem histórico).
     */
    List<Revision> findRevisions(UUID id);

    /**
     * Instantâneo de uma revisão Envers da janela: os valores dos campos auditados tal como
     * estavam nessa revisão, mais os metadados da própria revisão (número, tipo, data). O
     * servidor não calcula diferenças -- devolve instantâneos, a decisão de payload de 133-05.
     */
    record Revision(
            long revisionNumber,
            String revisionType, // INSERT | UPDATE | DELETE
            LocalDateTime revisionDate,
            LocalDate startDate,
            LocalDate endDate,
            Integer year,
            String status,
            String type,
            String purpose,
            LocalDateTime createdDate,
            String createdBy,
            String lastModifiedBy) {
    }
}
