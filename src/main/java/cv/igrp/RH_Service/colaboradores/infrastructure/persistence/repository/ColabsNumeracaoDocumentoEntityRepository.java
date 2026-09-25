package cv.igrp.RH_Service.colaboradores.infrastructure.persistence.repository;

import cv.igrp.RH_Service.colaboradores.infrastructure.persistence.entity.NumeracaoDocumentoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ColabsNumeracaoDocumentoEntityRepository extends JpaRepository<NumeracaoDocumentoEntity, String> {

    /** A série trancada até ao fim da transacção. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM ColabsNumeracaoDocumentoEntity n WHERE n.id = :id")
    Optional<NumeracaoDocumentoEntity> findParaActualizar(@Param("id") String id);
}
