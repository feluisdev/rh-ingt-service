package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.FeriasService;
import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobParametro;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Faz <b>vencer o direito a férias</b> — DL n.º 3/2010, art. 2.º n.º 4: «O direito a férias vence
 * no dia 1 de Janeiro de cada ano».
 *
 * <p>Corre todos os dias, e não uma vez por ano, por duas razões: no <b>ano de ingresso</b> o
 * direito cresce a cada trimestre completo de serviço (art. 3.º), e uma execução falhada a 1 de
 * Janeiro não pode deixar uma instituição inteira sem férias até ao ano seguinte. A passagem é
 * idempotente — só escreve quando há algo a mudar —, por isso repetir não custa nada e um dia sem
 * execução apanha-se no dia seguinte.
 *
 * <p><b>O ano é o do agendamento, não o de hoje.</b> A execução de 31 de Dezembro que falhou e só é
 * repetida a 2 de Janeiro trata o ano que acabou — é o {@code JobContext#dataReferencia()} que o
 * garante. O parâmetro {@code ano} permite tratar outro à mão.
 *
 * <p><b>Só colaboradores activos.</b> Quem cessou funções não vence férias novas; o que lhe era
 * devido à data da cessação é matéria do art. 12.º, que depende de remuneração e está fora do
 * âmbito desta aplicação.
 *
 * <p><b>Um erro num colaborador não derruba os outros</b>: a falha fica no log e na lista
 * {@code itensFalhados} da execução, e o lote segue — a execução termina {@code FALHA_PARCIAL}.
 *
 * <p>Agendado pelo framework de jobs ({@code shared/application/services/scheduler}). O cron vem de
 * {@code rh.ferias.vencimento.cron} (variável {@code RH_FERIAS_VENCIMENTO_CRON}) só da primeira vez;
 * depois de semeado, muda-se em {@code PUT /api/v1/rh/schedulers/RH_VENCIMENTO_FERIAS}.
 */
@Component
public class VencimentoFeriasJob implements ScheduledJob {

    public static final String CHAVE = "RH_VENCIMENTO_FERIAS";
    static final String PARAM_ANO = "ano";
    private static final Logger LOGGER = LoggerFactory.getLogger(VencimentoFeriasJob.class);
    private static final int TAMANHO_PAGINA = 100;

    private final FuncionarioRepository funcionarioRepository;
    private final FeriasService feriasService;
    private final String cronPadrao;

    public VencimentoFeriasJob(FuncionarioRepository funcionarioRepository,
                               FeriasService feriasService,
                               @Value("${rh.ferias.vencimento.cron:0 5 0 * * *}") String cronPadrao) {
        this.funcionarioRepository = funcionarioRepository;
        this.feriasService = feriasService;
        this.cronPadrao = cronPadrao;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Vencimento do direito a férias"; }

    /**
     * 00:05 por omissão — antes do job dos efeitos das licenças, para que a 1 de Janeiro o saldo já
     * exista quando o resto do dia começar.
     */
    @Override public String getCronPadrao()  { return cronPadrao; }

    /** Falhas deste job são tipicamente transitórias (base de dados); vale a pena repetir. */
    @Override public int getMaxTentativas() { return 3; }

    @Override
    public List<JobParametro> getParametros() {
        return List.of(JobParametro.ano(PARAM_ANO, "Ano")
                .ajuda("Ano do direito a vencer. Se vazio, usa o ano do agendamento."));
    }

    @Override
    public JobResult executar(JobContext ctx) {
        var explicito = ctx.getInteger(PARAM_ANO);
        int ano = explicito != null ? explicito : ctx.dataReferencia().getYear();
        LOGGER.info("Férias: a vencer o direito do ano {}...", ano);

        int pagina = 0;
        int processados = 0;
        int tratados = 0;
        var itensFalhados = new ArrayList<String>();
        List<Funcionario> lote;
        do {
            lote = paginaDeActivos(pagina);
            for (Funcionario funcionario : lote) {
                processados++;
                try {
                    if (feriasService.garantirSaldoDoAno(funcionario.getId(), ano).isPresent()) tratados++;
                } catch (Exception e) {
                    itensFalhados.add("%s (n.º %s): %s".formatted(
                            funcionario.getNomeCompleto(), funcionario.getNumeroFuncionario(), e.getMessage()));
                    LOGGER.error("Falhou o vencimento de férias do colaborador {}",
                            funcionario.getId().getStringValor(), e);
                }
            }
            pagina++;
        } while (lote.size() == TAMANHO_PAGINA);

        int semSaldo = processados - tratados - itensFalhados.size();
        LOGGER.info("Férias: {} colaboradores tratados para o ano {}.", tratados, ano);
        return JobResult.builder()
                .referencia(String.valueOf(ano))
                .processados(processados)
                .criados(tratados)
                .saltados(semSaldo)
                .falhas(itensFalhados.size())
                .detalhes(itensFalhados.isEmpty() ? null : Map.of("itensFalhados", itensFalhados))
                .mensagem(mensagem(ano, tratados, semSaldo, itensFalhados.size()))
                .build();
    }

    /**
     * {@code garantirSaldoDoAno} devolve vazio quando o catálogo não tem tipo de ausência de férias:
     * nesse caso nenhum colaborador fica com saldo, e a mensagem tem de o dizer em vez de parecer um
     * sucesso sem nada para fazer.
     */
    private static String mensagem(int ano, int tratados, int semSaldo, int falhas) {
        var texto = "%d colaboradores com o saldo de férias de %d em dia; %d falhas.".formatted(tratados, ano, falhas);
        if (semSaldo > 0)
            texto += " %d ficaram sem saldo — confirme que o catálogo tem o tipo de ausência de férias.".formatted(semSaldo);
        return texto;
    }

    private List<Funcionario> paginaDeActivos(int pagina) {
        FuncionarioFilter filtro = new FuncionarioFilter();
        filtro.setIsActive(true);
        filtro.setPage(pagina);
        filtro.setSize(TAMANHO_PAGINA);
        return funcionarioRepository.findAll(filtro);
    }
}
