package cv.igrp.RH_Service.funcionarios.domain.repository;
import cv.igrp.RH_Service.funcionarios.domain.filter.TipoDocumentoFilter;
import cv.igrp.RH_Service.funcionarios.domain.models.TipoDocumento;
import cv.igrp.RH_Service.shared.domain.valueobject.ExternalID;
import java.util.List;
import java.util.Optional;

public interface TipoDocumentoRepository {
    TipoDocumento save(TipoDocumento tipoDocumento);
    List<TipoDocumento> getAll();
    List<TipoDocumento> getAll(TipoDocumentoFilter filter);
    Optional<TipoDocumento> getByExternalId(ExternalID externalId);
}
