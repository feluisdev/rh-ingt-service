package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.ProcessoDisciplinarService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/**
 * <b>O dia a dia dos processos disciplinares</b> (BR-DIS-05, BR-DIS-15): executa as penas cujo dia chegou, conclui os
 * processos transitados e avisa o RH e o instrutor dos prazos que terminam em 2 dias ou terminaram ontem.
 */
@Component
public class PrazosDisciplinaresJob implements ScheduledJob {

    public static final String CHAVE = "RH_PRAZOS_DISCIPLINARES";

    private final ProcessoDisciplinarService service;

    public PrazosDisciplinaresJob(ProcessoDisciplinarService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Prazos e execução das penas dos processos disciplinares"; }
    @Override public String getCronPadrao()  { return "0 20 6 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        int executadas = service.executarDevidas(ctx.dataReferencia());
        int avisos = service.avisarPrazos(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(executadas + avisos)
                .mensagem(executadas + " pena(s) executada(s), " + avisos + " aviso(s) de prazo.").build();
    }
}
