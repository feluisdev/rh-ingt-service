package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.AuditRevision;

import java.util.List;
import java.util.UUID;

public interface AuditHistoryRepository {
    List<AuditRevision> findRevisions(String catalog, UUID entityId);
}
