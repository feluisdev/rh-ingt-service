package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.SaudeTrabalhoService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Validade dos exames de medicina do trabalho</b> (BR-SST-14): 30 dias antes e no dia a seguir a caducar. */
@Component
public class ExamesSaudeJob implements ScheduledJob {

    public static final String CHAVE = "RH_EXAMES_SAUDE";

    private final SaudeTrabalhoService service;

    public ExamesSaudeJob(SaudeTrabalhoService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Validade dos exames de medicina do trabalho"; }
    @Override public String getCronPadrao()  { return "0 15 7 * * *"; }
    @Override public java.util.List<JobParametro> getParametros() { return java.util.List.of(JobParametro.dataReferencia()); }

    @Override
    public JobResult executar(JobContext ctx) {
        int n = service.avisarValidades(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(n)
                .mensagem(n + " exame(s) a caducar ou caducado(s).").build();
    }
}
