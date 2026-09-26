package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.ChecklistService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Avisos das checklists</b> (BR-CHK-10): todos os dias, os itens pendentes cujo prazo terminou na véspera. */
@Component
public class AlertaChecklistsJob implements ScheduledJob {

    public static final String CHAVE = "RH_ALERTA_CHECKLISTS";

    private final ChecklistService service;

    public AlertaChecklistsJob(ChecklistService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Avisos de itens das checklists de entrada e saída fora do prazo"; }
    @Override public String getCronPadrao()  { return "0 50 6 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        int avisos = service.avisarPrazos(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(avisos)
                .mensagem(avisos + " checklist(s) com itens fora do prazo.").build();
    }
}
