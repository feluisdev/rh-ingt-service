package cv.igrp.RH_Service.colaboradores.infrastructure.scheduler;

import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.ReservaLugar;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReservaLugarRepository;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.domain.valueobject.PositionId;
import cv.igrp.RH_Service.shared.application.services.notificacoes.Notificador;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobContext;
import cv.igrp.RH_Service.shared.application.services.scheduler.JobResult;
import cv.igrp.RH_Service.shared.application.services.scheduler.ScheduledJob;
import cv.igrp.RH_Service.shared.domain.notificacoes.TipoNotificacao;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Reservas de Lugar paradas</b> (BR-AF-28): uma reserva prende um Lugar vago que mais ninguém pode ocupar. A cada
 * {@value #INTERVALO} dias sem contrato, o RH é avisado — registe o contrato ou cancele a reserva.
 */
@Component
public class AlertaReservasLugarJob implements ScheduledJob {

    public static final String CHAVE = "RH_ALERTA_RESERVAS_LUGAR";
    static final int INTERVALO = 30;
    /** Avisa aos 30, 60, … dias, até um ano. */
    static final int AVISOS = 12;

    private final ReservaLugarRepository reservaLugarRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final PositionRepository positionRepository;
    private final Notificador notificador;

    public AlertaReservasLugarJob(ReservaLugarRepository reservaLugarRepository, FuncionarioRepository funcionarioRepository,
                                  PositionRepository positionRepository, Notificador notificador) {
        this.reservaLugarRepository = reservaLugarRepository;
        this.funcionarioRepository = funcionarioRepository;
        this.positionRepository = positionRepository;
        this.notificador = notificador;
    }

    @Override public String getChave()       { return CHAVE; }
    @Override public String getNomeLegivel() { return "Avisos de Lugares reservados sem contrato"; }
    @Override public String getCronPadrao()  { return "0 45 6 * * *"; }

    @Override
    public JobResult executar(JobContext ctx) {
        LocalDate dia = ctx.dataReferencia();
        List<LocalDate> dias = new ArrayList<>();
        for (int i = 1; i <= AVISOS; i++) dias.add(dia.minusDays((long) INTERVALO * i));

        int avisos = 0;
        for (ReservaLugar r : reservaLugarRepository.findActivasReservadasEm(dias)) {
            String quem = funcionarioRepository.findById(r.getFuncionarioId()).map(Funcionario::getNomeCompleto)
                    .orElse("um colaborador");
            String lugar = positionRepository.findById(PositionId.from(r.getPositionId())).map(Position::getNumeroLugar)
                    .orElse("");
            notificador.paraRh().tipo(TipoNotificacao.LUGAR_RESERVADO)
                    .titulo("O Lugar " + lugar + " está reservado para " + quem + " desde " + Datas.pt(r.getReservadaEm())
                            + ", sem contrato")
                    .texto("Registe o contrato (o colaborador é colocado no Lugar) ou cancele a reserva, para libertar o Lugar.")
                    .recurso("RESERVA_LUGAR", r.getId().getStringValor()).enviar();
            avisos++;
        }
        return JobResult.builder().referencia(dia.toString()).criados(avisos)
                .mensagem(avisos + " aviso(s) de Lugares reservados sem contrato.").build();
    }
}
