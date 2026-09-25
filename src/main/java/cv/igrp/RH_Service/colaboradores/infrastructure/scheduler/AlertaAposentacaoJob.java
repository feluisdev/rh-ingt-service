package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.filter.FuncionarioFilter;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <b>Avisa quem se aproxima do limite de idade</b> (Lei n.º 20/X/2023, art. 48.º; BR-APO-04): 180, 90 e 30
 * dias antes do dia em que o vínculo cessa por idade (os 65 anos, ou o fim da prorrogação autorizada), e
 * no próprio dia. O RH recebe todos os avisos; o colaborador, o dos 180 dias e o do dia.
 *
 * <p>O dia é o do agendamento ({@code JobContext#dataReferencia()}): uma execução falhada e repetida
 * no dia seguinte avisa como se tivesse corrido a horas. Como cada aviso é para um número exacto de
 * dias, correr duas vezes no mesmo dia repete-o — o framework não deixa (uma execução por instante).
 */
@Component
public class AlertaAposentacaoJob implements ScheduledJob {

    public static final String CHAVE = "RH_ALERTA_APOSENTACAO";
    static final Set<Long> AVISOS = Set.of(180L, 90L, 30L, 0L);
    private static final Logger LOGGER = LoggerFactory.getLogger(AlertaAposentacaoJob.class);
    private static final int TAMANHO_PAGINA = 100;

    private final FuncionarioRepository funcionarioRepository;
    private final AposentacaoService aposentacaoService;
    private final Notificador notificador;

    public AlertaAposentacaoJob(FuncionarioRepository funcionarioRepository, AposentacaoService aposentacaoService,
                                Notificador notificador) {
        this.funcionarioRepository = funcionarioRepository;
        this.aposentacaoService = aposentacaoService;
        this.notificador = notificador;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Aviso do limite de idade (aposentação)"; }
    @Override public String getCronPadrao()  { return "0 30 6 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        LocalDate dia = ctx.dataReferencia();
        int processados = 0, avisados = 0;
        var falhas = new ArrayList<String>();
        int pagina = 0;
        List<Funcionario> lote;
        do {
            var filtro = new FuncionarioFilter();
            filtro.setIsActive(true);
            filtro.setPage(pagina++);
            filtro.setSize(TAMANHO_PAGINA);
            lote = funcionarioRepository.findAll(filtro);
            for (Funcionario f : lote) {
                processados++;
                try {
                    if (avisar(f, dia)) avisados++;
                } catch (Exception e) {
                    falhas.add("%s (n.º %s): %s".formatted(f.getNomeCompleto(), f.getNumeroFuncionario(), e.getMessage()));
                    LOGGER.error("Falhou o aviso de aposentação de {}", f.getId().getStringValor(), e);
                }
            }
        } while (lote.size() == TAMANHO_PAGINA);
        return JobResult.builder().referencia(dia.toString()).processados(processados).criados(avisados)
                .falhas(falhas.size()).detalhes(falhas.isEmpty() ? null : Map.of("itensFalhados", falhas))
                .mensagem(avisados + " aviso(s) de limite de idade.").build();
    }

    boolean avisar(Funcionario f, LocalDate dia) {
        LocalDate limite = aposentacaoService.limiteEfectivo(f);
        if (limite == null) return false;
        long faltam = ChronoUnit.DAYS.between(dia, limite);
        if (!AVISOS.contains(faltam)) return false;
        String quando = faltam == 0 ? "hoje (" + Datas.pt(limite) + ")" : "a " + Datas.pt(limite) + ", daqui a " + faltam + " dias";
        notificador.paraRh().tipo(TipoNotificacao.LIMITE_IDADE_PROXIMO)
                .titulo(f.getNomeCompleto() + " atinge o limite de idade " + quando)
                .texto("Abra o processo de aposentação ou registe a prorrogação autorizada.")
                .recurso("FUNCIONARIO", f.getId().getStringValor()).enviar();
        if (faltam == 180 || faltam == 0)
            notificador.para(f.getId()).tipo(TipoNotificacao.LIMITE_IDADE_PROXIMO)
                    .titulo("Atinge o limite de idade para o exercício de funções " + quando)
                    .texto("Fale com o RH sobre o processo de aposentação.")
                    .recurso("FUNCIONARIO", f.getId().getStringValor()).enviar();
        return true;
    }
}
