package cv.igrp.RH_Service.funcionarios.domain.repository;
import cv.igrp.RH_Service.funcionarios.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.Contrato;
import cv.igrp.RH_Service.funcionarios.domain.models.Documento;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository {
    Documento save(Documento documento);
    List<Documento> getAll();
    List<Documento> getAll(DocumentoFilter filter);
    Optional<Documento> getByExternalId(ExternalID externalId);
}
