package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.AcumulacaoFuncoesService;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.springframework.stereotype.Component;

/** <b>Acumulações de funções</b> (BR-ACU-07): caducam no fim do período; aviso 30 dias antes. */
@Component
public class AcumulacoesJob implements ScheduledJob {

    public static final String CHAVE = "RH_ACUMULACOES";

    private final AcumulacaoFuncoesService service;

    public AcumulacoesJob(AcumulacaoFuncoesService service) {
        this.service = service;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Fim das acumulações de funções"; }
    @Override public String getCronPadrao()  { return "0 5 7 * * *"; }
    @Override public java.util.List<JobParametro> getParametros() { return java.util.List.of(JobParametro.dataReferencia()); }

    @Override
    public JobResult executar(JobContext ctx) {
        int n = service.processar(ctx.dataReferencia());
        return JobResult.builder().referencia(ctx.dataReferencia().toString()).criados(n)
                .mensagem(n + " acumulação(ões) caducada(s) ou avisada(s).").build();
    }
}
