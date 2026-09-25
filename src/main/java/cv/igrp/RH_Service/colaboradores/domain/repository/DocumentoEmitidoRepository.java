package cv.igrp.RH_Service.colaboradores.domain.repository;

import cv.igrp.RH_Service.colaboradores.domain.models.DocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoEmitidoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;

import java.util.List;
import java.util.Optional;

public interface DocumentoEmitidoRepository {
    DocumentoEmitido save(DocumentoEmitido documento);
    Optional<DocumentoEmitido> findById(DocumentoEmitidoId id);
    Optional<DocumentoEmitido> findByCodigo(String codigoVerificacao);
    boolean existsByCodigo(String codigoVerificacao);
    /** Os de um colaborador, do mais recente para o mais antigo. */
    List<DocumentoEmitido> findByFuncionario(FuncionarioId funcionarioId);

    /**
     * O número seguinte da série no ano, reservado: a linha da série fica trancada até ao fim da transacção,
     * por isso duas réplicas nunca dão o mesmo número.
     */
    int proximoNumero(String serie, int ano);
}
