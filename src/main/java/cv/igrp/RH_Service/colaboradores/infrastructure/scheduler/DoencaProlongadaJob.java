package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Doença prolongada</b> (DL n.º 3/2010, art. 26.º; BR-SST-19): 30 dias seguidos de doença sugerem a junta médica. */
@Component
public class DoencaProlongadaJob implements ScheduledJob {

    public static final String CHAVE = "RH_DOENCA_PROLONGADA";

    private final SaudeTrabalhoService service;

    public DoencaProlongadaJob(SaudeTrabalhoService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Doença prolongada: sugerir a junta médica"; }
    @Override public String getCronPadrao()  { return "0 20 7 * * *"; }
    @Override public java.util.List<JobParametro> getParametros() { return java.util.List.of(JobParametro.dataReferencia()); }

    @Override
    public JobResult executar(JobContext ctx) {
        int n = service.avisarDoencaProlongada(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(n)
                .mensagem(n + " colaborador(es) com doença de 30 dias ou mais sem junta pedida.").build();
    }
}
