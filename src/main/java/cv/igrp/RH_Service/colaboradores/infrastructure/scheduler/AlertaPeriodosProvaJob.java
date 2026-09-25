package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.PeriodoProva;
import cv.igrp.RH_Service.colaboradores.domain.repository.ContratoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ProvimentoRepository;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * <b>Avisos da entrada ao serviço</b> (BR-PRV-13, BR-PRV-14), todos os dias, pelo dia do agendamento:
 * <ul>
 *   <li>a <b>30 dias</b> do fim de um estágio probatório ou período experimental — o tutor (o relatório) e o RH;</li>
 *   <li>no dia a seguir ao fim previsto, se continua EM_CURSO — o RH: falta a decisão;</li>
 *   <li>a <b>30 dias</b> do termo de um contrato — o RH: renovar (dentro do limite de renovações do tipo) ou deixar caducar.</li>
 * </ul>
 */
@Component
public class AlertaPeriodosProvaJob implements ScheduledJob {

    public static final String CHAVE = "RH_ALERTA_ENTRADA_SERVICO";
    static final int ANTECEDENCIA = 30;

    private final ProvimentoRepository provimentoRepository;
    private final ContratoRepository contratoRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final Notificador notificador;

    public AlertaPeriodosProvaJob(ProvimentoRepository provimentoRepository, ContratoRepository contratoRepository,
                                  FuncionarioRepository funcionarioRepository, Notificador notificador) {
        this.provimentoRepository = provimentoRepository;
        this.contratoRepository = contratoRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.notificador = notificador;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Avisos de períodos de prova e de fim de contrato"; }
    @Override public String getCronPadrao()  { return "0 40 6 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        LocalDate dia = ctx.dataReferencia();
        int avisos = 0;
        LocalDate aviso = dia.plusDays(ANTECEDENCIA);
        for (PeriodoProva p : provimentoRepository.findEmCursoComFimEntre(aviso, aviso)) {
            String quem = nome(p);
            String o = p.getTipo() == PeriodoProva.Tipo.ESTAGIO_PROBATORIO ? "O estágio probatório" : "O período experimental";
            notificador.paraRh().tipo(TipoNotificacao.PERIODO_PROVA)
                    .titulo(o + " de " + quem + " termina a " + Datas.pt(p.getFimPrevisto()))
                    .recurso("PERIODO_PROVA", p.getId().getStringValor()).enviar();
            if (p.getTutorId() != null)
                notificador.para(p.getTutorId()).tipo(TipoNotificacao.PERIODO_PROVA)
                        .titulo("Remeta o relatório final do estágio de " + quem + " (termina a " + Datas.pt(p.getFimPrevisto()) + ")")
                        .recurso("PERIODO_PROVA", p.getId().getStringValor()).enviar();
            avisos++;
        }
        LocalDate ontem = dia.minusDays(1);
        for (PeriodoProva p : provimentoRepository.findEmCursoComFimEntre(ontem, ontem)) {
            notificador.paraRh().tipo(TipoNotificacao.PERIODO_PROVA)
                    .titulo("Terminou o período de prova de " + nome(p) + " e falta a decisão")
                    .texto(p.getAvaliacao() != null ? "O tutor já remeteu o relatório." : "O tutor ainda não remeteu o relatório.")
                    .recurso("PERIODO_PROVA", p.getId().getStringValor()).enviar();
            avisos++;
        }
        for (var c : contratoRepository.findCorrentesComFimEntre(aviso, aviso)) {
            String quem = funcionarioRepository.findById(c.getFuncionarioId()).map(Funcionario::getNomeCompleto).orElse("um colaborador");
            notificador.paraRh().tipo(TipoNotificacao.CONTRATO_TERMO_A_TERMINAR)
                    .titulo("O contrato de " + quem + " termina a " + Datas.pt(c.getEndDate()))
                    .texto("Renove-o (renovações feitas: " + (c.getRenewalCount() != null ? c.getRenewalCount() : 0)
                            + ") ou deixe-o caducar.")
                    .recurso("CONTRATO", c.getId().getStringValor()).enviar();
            avisos++;
        }
        return JobResult.builder().referencia(dia.toString()).criados(avisos).mensagem(avisos + " aviso(s) da entrada ao serviço.").build();
    }

    private String nome(PeriodoProva p) {
        return funcionarioRepository.findById(p.getFuncionarioId()).map(Funcionario::getNomeCompleto).orElse("um colaborador");
    }
}
