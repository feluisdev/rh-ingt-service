package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.ComissaoServicoService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Aviso do termo das comissões de serviço</b> (BR-CMS-07): 90 dias antes, para renovar ou deixar terminar. */
@Component
public class AlertaComissoesServicoJob implements ScheduledJob {

    public static final String CHAVE = "RH_ALERTA_COMISSOES_SERVICO";

    private final ComissaoServicoService service;

    public AlertaComissoesServicoJob(ComissaoServicoService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Aviso do termo das comissões de serviço"; }
    @Override public String getCronPadrao()  { return "0 55 6 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        int avisos = service.avisarTermos(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(avisos)
                .mensagem(avisos + " comissão(ões) de serviço a terminar daqui a " + ComissaoServicoService.DIAS_AVISO_TERMO + " dias.").build();
    }
}
