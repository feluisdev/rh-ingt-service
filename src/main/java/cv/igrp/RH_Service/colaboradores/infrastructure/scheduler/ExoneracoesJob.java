package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.ExoneracaoService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Exonerações do dia</b> (BR-EXO-05): as deferidas produzem efeitos no dia devido; as pedidas sem despacho lembram o RH. */
@Component
public class ExoneracoesJob implements ScheduledJob {

    public static final String CHAVE = "RH_EXONERACOES";

    private final ExoneracaoService service;

    public ExoneracoesJob(ExoneracaoService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Efeitos das exonerações voluntárias"; }
    @Override public String getCronPadrao()  { return "0 10 6 * * *"; }
    @Override public java.util.List<JobParametro> getParametros() { return java.util.List.of(JobParametro.dataReferencia()); }

    @Override
    public JobResult executar(JobContext ctx) {
        int n = service.processarDevidas(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(n)
                .mensagem(n + " exoneração(ões) produziram efeitos.").build();
    }
}
