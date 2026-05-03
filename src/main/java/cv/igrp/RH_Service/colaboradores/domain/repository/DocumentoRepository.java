package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Documento;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository {
    Documento save(Documento documento);
    Optional<Documento> findById(DocumentoId id);
    List<Documento> findAllByFuncionarioId(FuncionarioId funcionarioId, DocumentoFilter filter);
}
