package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.DocumentoEmitidoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ColabsDocumentoEmitidoEntityRepository extends JpaRepository<DocumentoEmitidoEntity, UUID> {

    Optional<DocumentoEmitidoEntity> findByCodigoVerificacao(String codigoVerificacao);

    boolean existsByCodigoVerificacao(String codigoVerificacao);

    @Query("""
            SELECT d FROM ColabsDocumentoEmitidoEntity d
            WHERE d.funcionario.id = :funcionarioId
            ORDER BY d.emitidoEm DESC""")
    List<DocumentoEmitidoEntity> findDoFuncionario(@Param("funcionarioId") UUID funcionarioId);
}
