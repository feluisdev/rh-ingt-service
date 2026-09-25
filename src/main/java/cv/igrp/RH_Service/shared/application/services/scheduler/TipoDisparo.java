package cv.igrp.RH_Service.shared.application.services.scheduler;

/**
 * Origem de uma execução de job: agendada pelo cron ou disparada manualmente via API.
 */
public enum TipoDisparo {
    AGENDADO,
    MANUAL
}
