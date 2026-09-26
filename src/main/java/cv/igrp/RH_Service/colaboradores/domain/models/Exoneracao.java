package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.valueobject.ExoneracaoId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

/**
 * <b>A exoneração voluntária</b> (Lei n.º 20/X/2023, arts. 94.º e 95.º; BR-EXO-01..08): a pedido do funcionário, com
 * <b>pré-aviso de 60 dias</b>; pode ser <b>condicionada</b> (processo disciplinar em que é arguido, inquérito ou
 * sindicância, prazos de garantia de formação por cumprir) e então produz efeitos logo que a causa cesse ou, no máximo,
 * <b>90 dias</b> depois do pré-aviso (art. 94.º n.os 4 e 5 a)).
 *
 * <p>PEDIDA → DEFERIDA (despacho, publicado) → EFECTIVADA (a cessação do vínculo, no dia de efeito); DESISTIDA antes.
 */
@Getter
public class Exoneracao {

    public enum Estado { PEDIDA, DEFERIDA, EFECTIVADA, DESISTIDA }

    /** Art. 94.º n.º 4. */
    public static final int DIAS_PRE_AVISO = 60;
    /** Art. 94.º n.os 4 e 5 a): o máximo que a condição adia. */
    public static final int DIAS_LIMITE = 90;

    private ExoneracaoId id;
    private FuncionarioId funcionarioId;
    private LocalDate dataPreAviso;
    private LocalDate dataPretendida;
    private String motivo;
    private boolean pedidaPeloProprio;
    private Estado estado;
    private String despacho;
    private LocalDate dataDespacho;
    /** O último dia em que a exoneração estava condicionada (a causa cessou depois dele). */
    private LocalDate condicionadaAte;
    private LocalDate dataEfeito;

    private Exoneracao() {}

    public static Exoneracao pedir(FuncionarioId funcionarioId, LocalDate preAviso, LocalDate pretendida, String motivo, boolean peloProprio,
                                   LocalDate hoje) {
        LocalDate aviso = preAviso != null ? preAviso : hoje;
        if (aviso.isAfter(hoje)) throw invalido("A data do pré-aviso não pode ser no futuro.");
        LocalDate minima = aviso.plusDays(DIAS_PRE_AVISO);
        LocalDate data = pretendida != null ? pretendida : minima;
        if (data.isBefore(minima))
            throw invalido("O pré-aviso é de 60 dias: com o pedido a " + Datas.pt(aviso) + ", a exoneração produz efeitos a partir de "
                    + Datas.pt(minima) + " (art. 94.º n.º 4).");
        var e = new Exoneracao();
        e.id = ExoneracaoId.gerarNovo();
        e.funcionarioId = funcionarioId;
        e.dataPreAviso = aviso;
        e.dataPretendida = data;
        e.motivo = motivo != null && !motivo.isBlank() ? motivo.trim() : null;
        e.pedidaPeloProprio = peloProprio;
        e.estado = Estado.PEDIDA;
        return e;
    }

    public static Exoneracao reconstruir(ExoneracaoId id, FuncionarioId funcionarioId, LocalDate dataPreAviso, LocalDate dataPretendida,
                                         String motivo, boolean pedidaPeloProprio, Estado estado, String despacho, LocalDate dataDespacho,
                                         LocalDate condicionadaAte, LocalDate dataEfeito) {
        var e = new Exoneracao();
        e.id = id;
        e.funcionarioId = funcionarioId;
        e.dataPreAviso = dataPreAviso;
        e.dataPretendida = dataPretendida;
        e.motivo = motivo;
        e.pedidaPeloProprio = pedidaPeloProprio;
        e.estado = estado;
        e.despacho = despacho;
        e.dataDespacho = dataDespacho;
        e.condicionadaAte = condicionadaAte;
        e.dataEfeito = dataEfeito;
        return e;
    }

    /** O despacho que concede a exoneração. */
    public void deferir(String despacho, LocalDate data) {
        if (estado != Estado.PEDIDA) throw IgrpResponseStatusException.conflict("Esta exoneração já foi decidida.");
        if (despacho == null || despacho.isBlank()) throw invalido("Indique o despacho de exoneração.");
        if (data == null || data.isBefore(dataPreAviso)) throw invalido("O despacho não pode ser antes do pedido.");
        this.despacho = despacho.trim();
        this.dataDespacho = data;
        this.estado = Estado.DEFERIDA;
    }

    public LocalDate dataLimite() {
        return dataPreAviso.plusDays(DIAS_LIMITE);
    }

    /** Neste dia ainda está condicionada: fica registado (a causa cessa, quando cessar, depois dele). */
    public void registarCondicionada(LocalDate dia) {
        if (condicionadaAte == null || dia.isAfter(condicionadaAte)) this.condicionadaAte = dia;
    }

    /**
     * O dia em que a exoneração deferida produz efeitos, se já chegou (BR-EXO-04): sem condição, na data pretendida (ou no
     * dia a seguir a a condição cessar, se foi depois); condicionada, no limite dos 90 dias. Nulo se ainda não é o dia.
     */
    public LocalDate efeitoDevido(boolean condicionadaHoje, LocalDate hoje) {
        if (estado != Estado.DEFERIDA) return null;
        if (condicionadaHoje) return hoje.isBefore(dataLimite()) ? null : dataLimite();
        LocalDate efeito = dataPretendida;
        if (condicionadaAte != null && !condicionadaAte.isBefore(efeito)) efeito = condicionadaAte.plusDays(1);
        if (efeito.isAfter(dataLimite())) efeito = dataLimite();
        return hoje.isBefore(efeito) ? null : efeito;
    }

    public void efectivar(LocalDate data) {
        if (estado != Estado.DEFERIDA) throw IgrpResponseStatusException.conflict("Só uma exoneração deferida produz efeitos.");
        this.dataEfeito = data;
        this.estado = Estado.EFECTIVADA;
    }

    public void desistir() {
        if (estado != Estado.PEDIDA && estado != Estado.DEFERIDA) throw IgrpResponseStatusException.conflict("Esta exoneração já terminou.");
        this.estado = Estado.DESISTIDA;
    }

    public boolean emCurso() {
        return estado == Estado.PEDIDA || estado == Estado.DEFERIDA;
    }

    static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
