package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.MissaoServicoService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Relatórios de missão em falta</b> (BR-MSS-09): no dia a seguir ao regresso previsto de uma missão ainda sem relatório. */
@Component
public class AlertaMissoesServicoJob implements ScheduledJob {

    public static final String CHAVE = "RH_ALERTA_MISSOES_SERVICO";

    private final MissaoServicoService service;

    public AlertaMissoesServicoJob(MissaoServicoService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Lembrete do relatório das missões de serviço"; }
    @Override public String getCronPadrao()  { return "0 0 7 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        int n = service.lembrarRelatorios(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(n)
                .mensagem(n + " missão(ões) sem relatório de regresso.").build();
    }
}
