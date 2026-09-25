package cv.igrp.RH_Service.shared.application.services.notificacoes;

import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import cv.igrp.RH_Service.shared.infrastructure.persistence.repository.notificacoes.NotificacaoEnvioEntityRepository;
import org.springframework.stereotype.Component;

/**
 * Despacha a fila de envio das notificações por correio electrónico ({@code t_notificacao_envio}).
 *
 * <p><b>{@code TODO(smtp)}</b>: ainda não há canal. O job existe — aparece na interface dos jobs e pode
 * ser disparado à mão — mas nasce <b>desactivado</b> e, quando corre, só conta o que está à espera.
 * Quando o SMTP (ou a aplicação de notificações da instituição) existir, é aqui que se envia: ler as
 * PENDENTE por ordem, resolver o endereço do destinatário, enviar, marcar ENVIADO ou FALHOU (com
 * {@code tentativas} e {@code ultimo_erro}).
 */
@Component
public class EnvioNotificacoesJob implements ScheduledJob {

    public static final String CHAVE = "RH_ENVIO_NOTIFICACOES";

    private final NotificacaoEnvioEntityRepository envioRepository;

    public EnvioNotificacoesJob(NotificacaoEnvioEntityRepository envioRepository) {
        this.envioRepository = envioRepository;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Envio de notificações por correio electrónico"; }
    @Override public String getCronPadrao()  { return "0 */10 * * * *"; }
    @Override public boolean isActivoPorOmissao() { return false; }

    @Override
    public JobResult executar(JobContext ctx) {
        long pendentes = envioRepository.countByEstado("PENDENTE");
        // TODO(smtp): enviar as PENDENTE quando houver canal de correio electrónico.
        return JobResult.builder()
                .processados((int) Math.min(pendentes, Integer.MAX_VALUE))
                .saltados((int) Math.min(pendentes, Integer.MAX_VALUE))
                .mensagem("Sem canal de correio electrónico configurado: " + pendentes + " envio(s) à espera.")
                .build();
    }
}
