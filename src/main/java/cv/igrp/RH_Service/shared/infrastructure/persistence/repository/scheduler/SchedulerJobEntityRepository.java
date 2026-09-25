package cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerJobEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchedulerJobEntityRepository extends JpaRepository<SchedulerJobEntity, UUID> {

    Optional<SchedulerJobEntity> findByChave(String chave);

    /**
     * A linha do job, bloqueada até ao fim da transacção ({@code SELECT … FOR UPDATE}). É o lock
     * entre réplicas: quem abre uma execução bloqueia primeiro esta linha, e a segunda réplica só
     * faz a sua verificação depois de a primeira ter gravado o registo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select j from SchedulerJobEntity j where j.chave = :chave")
    Optional<SchedulerJobEntity> findByChaveParaActualizar(@Param("chave") String chave);

    List<SchedulerJobEntity> findByActivoTrue();
}
