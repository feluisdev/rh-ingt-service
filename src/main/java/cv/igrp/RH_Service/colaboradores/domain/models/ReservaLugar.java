package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReservaLugarId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import lombok.Getter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * <b>Reserva de um Lugar</b> para quem foi registado sem contrato (BR-AF-23 a BR-AF-28). O Lugar fica guardado para
 * essa pessoa, mas <b>não é ocupado</b>: não há afectação, nem datas no Lugar, e tudo o que deriva da ocupação
 * (vagas, mapa de pessoal, chefias, SIADAP, movimentos) continua a ver o Lugar vago e a pessoa sem Lugar — que é
 * a verdade, porque sem vínculo não há Lugar (BR-AF-18). Quando o contrato é registado, a reserva passa a
 * colocação, pelas regras de sempre.
 *
 * <p>Não é um {@link TipoAfectacao}: esse é o título com que alguém <i>ocupa</i> um Lugar, e a reserva não ocupa.
 * Vive à parte para que nenhuma leitura de afectações a possa confundir com uma.
 *
 * <p>ACTIVA → CONCLUIDA (colocado) | CANCELADA.
 */
@Getter
public class ReservaLugar {

    public enum Estado { ACTIVA, CONCLUIDA, CANCELADA }

    private ReservaLugarId id;
    private FuncionarioId funcionarioId;
    private UUID positionId;
    private UUID gradeId;
    private UUID functionId;
    private String notes;
    private Estado estado;
    private LocalDate reservadaEm;
    private LocalDate fechadaEm;
    /** Porque foi cancelada; nulo nas concluídas. */
    private String motivo;
    /** A afectação em que a reserva se concretizou. */
    private UUID assignmentId;

    private ReservaLugar() {}

    public static ReservaLugar reservar(FuncionarioId funcionarioId, UUID positionId, UUID gradeId, UUID functionId,
                                       String notes, LocalDate hoje) {
        if (funcionarioId == null || positionId == null)
            throw IgrpResponseStatusException.badRequest("Indique o colaborador e o Lugar a reservar.");
        var r = new ReservaLugar();
        r.id = ReservaLugarId.gerarNovo();
        r.funcionarioId = funcionarioId;
        r.positionId = positionId;
        r.gradeId = gradeId;
        r.functionId = functionId;
        r.notes = notes;
        r.estado = Estado.ACTIVA;
        r.reservadaEm = hoje;
        return r;
    }

    public static ReservaLugar reconstruir(ReservaLugarId id, FuncionarioId funcionarioId, UUID positionId, UUID gradeId,
                                           UUID functionId, String notes, Estado estado, LocalDate reservadaEm,
                                           LocalDate fechadaEm, String motivo, UUID assignmentId) {
        var r = new ReservaLugar();
        r.id = id;
        r.funcionarioId = funcionarioId;
        r.positionId = positionId;
        r.gradeId = gradeId;
        r.functionId = functionId;
        r.notes = notes;
        r.estado = estado;
        r.reservadaEm = reservadaEm;
        r.fechadaEm = fechadaEm;
        r.motivo = motivo;
        r.assignmentId = assignmentId;
        return r;
    }

    /** O colaborador foi colocado no Lugar reservado. */
    public void concluir(UUID assignmentId, LocalDate data) {
        exigirActiva();
        this.estado = Estado.CONCLUIDA;
        this.assignmentId = assignmentId;
        this.fechadaEm = data;
    }

    public void cancelar(String motivo, LocalDate data) {
        exigirActiva();
        if (motivo == null || motivo.isBlank())
            throw IgrpResponseStatusException.badRequest("Indique o motivo do cancelamento da reserva.");
        this.estado = Estado.CANCELADA;
        this.motivo = motivo.trim();
        this.fechadaEm = data;
    }

    public boolean activa() {
        return estado == Estado.ACTIVA;
    }

    private void exigirActiva() {
        if (!activa())
            throw IgrpResponseStatusException.conflict("Esta reserva já não está activa.");
    }
}
