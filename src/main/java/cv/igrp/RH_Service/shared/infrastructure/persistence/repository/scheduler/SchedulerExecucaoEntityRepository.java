package cv.igrp.RH_Service.shared.infrastructure.persistence.repository.scheduler;

import cv.igrp.RH_Service.shared.infrastructure.persistence.entity.scheduler.SchedulerExecucaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SchedulerExecucaoEntityRepository
        extends JpaRepository<SchedulerExecucaoEntity, UUID>, JpaSpecificationExecutor<SchedulerExecucaoEntity> {

    /** Guarda de concorrência: já corre alguma execução desta chave? */
    boolean existsByChaveAndEstado(String chave, String estado);

    /** Deduplicação do disparo e das omissões: já existe registo para este instante agendado? */
    boolean existsByChaveAndAgendadoPara(String chave, LocalDateTime agendadoPara);

    /** Execuções presas em A_CORRER — candidatas a zombie. */
    List<SchedulerExecucaoEntity> findByEstado(String estado);

    /** Execuções A_CORRER de uma réplica concreta — as que ela deixou a meio ao morrer. */
    List<SchedulerExecucaoEntity> findByEstadoAndInstancia(String estado, String instancia);
}
