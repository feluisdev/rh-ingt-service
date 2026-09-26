package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FechoMensalId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * <b>O fecho de um mês de processamento</b> (DL n.º 3/2010, art. 75.º — a relação mensal; BR-FEC-01..08). Fechar congela a
 * relação mensal do mês (fica guardada) e o conjunto de factos que o salarial recebe; depois de fechado, os factos com efeito
 * nesse mês entram no mês aberto seguinte, como ajuste [ind.]. Reabrir exige motivo; fecha-se de novo com nova fotografia.
 *
 * <p>FECHADO ⇄ REABERTO.
 */
@Getter
public class FechoMensal {

    public enum Estado { FECHADO, REABERTO }

    private FechoMensalId id;
    private YearMonth mes;
    private Estado estado;
    private LocalDateTime fechadoEm;
    private int fechos;
    private String motivoReabertura;
    private LocalDateTime reabertoEm;
    /** A relação mensal no momento do fecho (JSON). */
    private String relacao;
    private int totalFactos;

    private FechoMensal() {}

    public static FechoMensal fechar(YearMonth mes, YearMonth corrente, String relacao, int totalFactos, LocalDateTime agora) {
        if (mes == null) throw invalido("Indique o mês.");
        if (mes.isAfter(corrente)) throw invalido("Não se fecha um mês que ainda não começou.");
        var f = new FechoMensal();
        f.id = FechoMensalId.gerarNovo();
        f.mes = mes;
        f.estado = Estado.FECHADO;
        f.fechadoEm = agora;
        f.fechos = 1;
        f.relacao = relacao;
        f.totalFactos = totalFactos;
        return f;
    }

    public static FechoMensal reconstruir(FechoMensalId id, YearMonth mes, Estado estado, LocalDateTime fechadoEm, int fechos,
                                          String motivoReabertura, LocalDateTime reabertoEm, String relacao, int totalFactos) {
        var f = new FechoMensal();
        f.id = id;
        f.mes = mes;
        f.estado = estado;
        f.fechadoEm = fechadoEm;
        f.fechos = fechos;
        f.motivoReabertura = motivoReabertura;
        f.reabertoEm = reabertoEm;
        f.relacao = relacao;
        f.totalFactos = totalFactos;
        return f;
    }

    /** Fechar de novo um mês reaberto: nova fotografia. */
    public void fecharDeNovo(String relacao, int totalFactos, LocalDateTime agora) {
        if (estado == Estado.FECHADO) throw IgrpResponseStatusException.conflict("Este mês já está fechado.");
        this.estado = Estado.FECHADO;
        this.fechadoEm = agora;
        this.fechos++;
        this.relacao = relacao;
        this.totalFactos = totalFactos;
    }

    public void reabrir(String motivo, LocalDateTime agora) {
        if (estado != Estado.FECHADO) throw IgrpResponseStatusException.conflict("Este mês não está fechado.");
        if (motivo == null || motivo.isBlank()) throw invalido("Reabrir um mês fechado exige o motivo (o salarial pode já o ter processado).");
        this.estado = Estado.REABERTO;
        this.motivoReabertura = motivo.trim();
        this.reabertoEm = agora;
    }

    public boolean fechado() {
        return estado == Estado.FECHADO;
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
