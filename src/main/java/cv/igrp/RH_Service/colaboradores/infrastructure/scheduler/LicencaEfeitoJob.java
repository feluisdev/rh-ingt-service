package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.LicencaEfeitoService;
import cv.igrp.RH_Service.colaboradores.domain.models.LicencaMobilidade;
import cv.igrp.RH_Service.colaboradores.domain.repository.LicencaMobilidadeRepository;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Job diário que dá aos efeitos das licenças a sua <b>data efectiva</b>.
 *
 * <p>Uma licença deferida hoje para começar em Outubro só produz efeitos em Outubro; e uma
 * licença cujo fim passou termina nesse dia, sem esperar por ninguém — é o «caduca
 * automaticamente» do art. 46.º n.º 3 do DL n.º 3/2010. Antes da V48 nada disto acontecia
 * sozinho: os efeitos eram aplicados na aprovação, e o fim só chegava quando alguém carregasse
 * no {@code /close}. Uma licença esquecida ficava em vigor para sempre.
 *
 * <p><b>Repetir é seguro, e faltar um dia não perde nada.</b> A pergunta é feita ao estado actual
 * — o que está deferido, já começou e ainda não teve efeitos — e não a um intervalo desde a
 * última execução. Se a aplicação estiver em baixo três dias, a passagem seguinte apanha o
 * atraso; e o regresso é aplicado com a <b>data de fim da licença</b>, não com a data em que o
 * job correu, para que a contagem de dias não dependa de quando a máquina esteve de pé. Essa
 * contagem é a base do desconto na antiguidade e das férias proporcionais (art. 47.º n.os 1 a 3).
 *
 * <p><b>Um erro num registo não derruba os outros.</b> Cada licença é tratada por si: a falha fica
 * no log e na lista {@code itensFalhados} da execução, e o lote segue — a execução termina
 * {@code FALHA_PARCIAL}.
 *
 * <p>Agendado pelo framework de jobs ({@code shared/application/services/scheduler}): a guarda entre
 * réplicas, o registo de cada execução e o disparo manual são dele. O cron vem de
 * {@code rh.licencas.efeitos.cron} (variável {@code RH_LICENCAS_EFEITOS_CRON}) só da primeira vez;
 * depois de semeado, muda-se em {@code PUT /api/v1/rh/schedulers/RH_EFEITOS_LICENCAS}.
 */
@Component
public class LicencaEfeitoJob implements ScheduledJob {

    public static final String CHAVE = "RH_EFEITOS_LICENCAS";
    private static final Logger LOGGER = LoggerFactory.getLogger(LicencaEfeitoJob.class);

    private final LicencaMobilidadeRepository licencaRepository;
    private final LicencaEfeitoService efeitoService;
    private final String cronPadrao;

    public LicencaEfeitoJob(LicencaMobilidadeRepository licencaRepository,
                            LicencaEfeitoService efeitoService,
                            @Value("${rh.licencas.efeitos.cron:0 15 0 * * *}") String cronPadrao) {
        this.licencaRepository = licencaRepository;
        this.efeitoService = efeitoService;
        this.cronPadrao = cronPadrao;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Efeitos das licenças e mobilidades"; }

    /** 00:15 por omissão, depois do vencimento de férias (00:05) e antes do movimento do dia. */
    @Override public String getCronPadrao()  { return cronPadrao; }

    @Override
    public List<JobParametro> getParametros() {
        return List.of(JobParametro.dataReferencia()
                .ajuda("Dia para o qual os efeitos são aplicados. Se vazio, usa o dia do agendamento."));
    }

    @Override
    public JobResult executar(JobContext ctx) {
        LocalDate hoje = ctx.dataReferencia();
        LOGGER.info("Licenças/mobilidades: a aplicar os efeitos devidos a {}...", hoje);

        var contagem = new Contagem();
        aplicar(licencaRepository.findEntradaPorAplicar(hoje), hoje, true, contagem);
        int entradas = contagem.aplicados;
        aplicar(licencaRepository.findRegressoPorAplicar(hoje), hoje, false, contagem);
        int regressos = contagem.aplicados - entradas;

        LOGGER.info("Licenças/mobilidades: {} entradas e {} regressos aplicados, {} falhas.",
                entradas, regressos, contagem.itensFalhados.size());
        return JobResult.builder()
                .referencia(hoje.toString())
                .processados(contagem.processados)
                .criados(contagem.aplicados)
                .saltados(contagem.processados - contagem.aplicados - contagem.itensFalhados.size())
                .falhas(contagem.itensFalhados.size())
                .detalhes(contagem.itensFalhados.isEmpty() ? null : Map.of("itensFalhados", contagem.itensFalhados))
                .mensagem("%d entradas em vigor e %d regressos aplicados; %d falhas."
                        .formatted(entradas, regressos, contagem.itensFalhados.size()))
                .build();
    }

    private void aplicar(List<LicencaMobilidade> licencas, LocalDate hoje, boolean entrada, Contagem contagem) {
        for (LicencaMobilidade licenca : licencas) {
            contagem.processados++;
            try {
                var efeito = entrada
                        ? efeitoService.aplicarEntradaSeDevida(licenca, hoje)
                        : efeitoService.aplicarRegressoSeDevido(licenca, hoje);
                if (efeito.aplicado()) contagem.aplicados++;
            } catch (Exception e) {
                var accao = entrada ? "a entrada em vigor" : "o regresso";
                // O contador diz quantos falharam; isto diz QUAIS — é o que é preciso para corrigir e repetir.
                contagem.itensFalhados.add("Licença %s (%s): %s"
                        .formatted(licenca.getId().getStringValor(), accao, e.getMessage()));
                LOGGER.error("Falhou {} da licença/mobilidade {}", accao, licenca.getId().getStringValor(), e);
            }
        }
    }

    private static final class Contagem {
        int processados;
        int aplicados;
        final List<String> itensFalhados = new ArrayList<>();
    }
}
