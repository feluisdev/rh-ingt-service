package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.AutosAssiduidadeService;
import cv.igrp.RH_Service.colaboradores.application.services.ProcessoDisciplinarService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/**
 * <b>O dia a dia dos processos disciplinares</b> (BR-DIS-05, BR-DIS-15): executa as penas cujo dia chegou, conclui os
 * processos transitados, avisa o RH e o instrutor dos prazos que terminam em 2 dias ou terminaram ontem, e avisa dos autos
 * por falta de assiduidade ou abandono de lugar a levantar (BR-DIS-25).
 */
@Component
public class PrazosDisciplinaresJob implements ScheduledJob {

    public static final String CHAVE = "RH_PRAZOS_DISCIPLINARES";

    private final ProcessoDisciplinarService service;
    private final AutosAssiduidadeService autos;

    public PrazosDisciplinaresJob(ProcessoDisciplinarService service, AutosAssiduidadeService autos) {
        this.service = service;
        this.autos = autos;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Prazos e execução das penas dos processos disciplinares"; }
    @Override public String getCronPadrao()  { return "0 20 6 * * *"; }
    @Override public java.util.List<JobParametro> getParametros() { return java.util.List.of(JobParametro.dataReferencia()); }

    @Override
    public JobResult executar(JobContext ctx) {
        int executadas = service.executarDevidas(ctx.dataReferencia());
        int avisos = service.avisarPrazos(ctx.dataReferencia());
        int sugeridos = autos.avisar(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(executadas + avisos + sugeridos)
                .mensagem(executadas + " pena(s) executada(s), " + avisos + " aviso(s) de prazo, " + sugeridos + " auto(s) a levantar.").build();
    }
}
