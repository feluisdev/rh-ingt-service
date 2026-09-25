package cv.igrp.RH_Service.colaboradores.domain.models;

import cv.igrp.RH_Service.colaboradores.domain.service.RegrasAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ProrrogacaoPermanenciaId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.Datas;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Objects;

/**
 * <b>Permanência ao serviço para além dos 65 anos</b> (Lei n.º 20/X/2023, art. 48.º n.os 2 e 3; BR-APO-03):
 * por interesse público excepcional devidamente fundamentado, até aos 70. Depende da <b>vontade do
 * funcionário</b>, de <b>proposta fundamentada</b> do membro do Governo do serviço e da <b>autorização</b>
 * do membro do Governo da Administração Pública — o despacho regista-se aqui.
 */
@Getter
public class ProrrogacaoPermanencia {

    public enum Estado { PEDIDA, AUTORIZADA, INDEFERIDA }

    private ProrrogacaoPermanenciaId id;
    private FuncionarioId funcionarioId;
    private Estado estado;
    private LocalDate dataPedido;
    private String propostaFundamentada;
    /** Até quando se mantém ao serviço; nunca depois do dia em que faz 70 anos. */
    private LocalDate validaAte;
    private String despachoNumero;
    private LocalDate despachoData;
    private String motivoIndeferimento;

    private ProrrogacaoPermanencia() {}

    public static ProrrogacaoPermanencia pedir(FuncionarioId funcionarioId, LocalDate dataNascimento, LocalDate dataPedido,
                                               boolean manifestacaoVontade, String propostaFundamentada,
                                               LocalDate validaAte) {
        Objects.requireNonNull(funcionarioId);
        if (!manifestacaoVontade)
            throw invalido("A permanência para além dos 65 anos depende da vontade do funcionário. Registe a sua manifestação de vontade.");
        if (propostaFundamentada == null || propostaFundamentada.isBlank())
            throw invalido("A permanência para além dos 65 anos exige proposta fundamentada do interesse público excepcional.");
        if (validaAte == null) throw invalido("Indique até quando se mantém ao serviço.");
        LocalDate setenta = RegrasAposentacao.faz(dataNascimento, RegrasAposentacao.IDADE_MAXIMA);
        if (setenta != null && validaAte.isAfter(setenta))
            throw invalido("Ninguém se mantém ao serviço depois dos 70 anos. Este colaborador faz 70 anos em "
                    + Datas.pt(setenta) + ".");
        LocalDate sessentaCinco = RegrasAposentacao.faz(dataNascimento, RegrasAposentacao.IDADE_LIMITE);
        if (sessentaCinco != null && !validaAte.isAfter(sessentaCinco))
            throw invalido("A prorrogação é para depois dos 65 anos, que este colaborador faz em " + Datas.pt(sessentaCinco) + ".");
        var p = new ProrrogacaoPermanencia();
        p.id = ProrrogacaoPermanenciaId.gerarNovo();
        p.funcionarioId = funcionarioId;
        p.estado = Estado.PEDIDA;
        p.dataPedido = Objects.requireNonNull(dataPedido);
        p.propostaFundamentada = propostaFundamentada.trim();
        p.validaAte = validaAte;
        return p;
    }

    public static ProrrogacaoPermanencia reconstruir(ProrrogacaoPermanenciaId id, FuncionarioId funcionarioId, Estado estado,
                                                     LocalDate dataPedido, String propostaFundamentada, LocalDate validaAte,
                                                     String despachoNumero, LocalDate despachoData, String motivoIndeferimento) {
        var p = new ProrrogacaoPermanencia();
        p.id = id;
        p.funcionarioId = funcionarioId;
        p.estado = estado;
        p.dataPedido = dataPedido;
        p.propostaFundamentada = propostaFundamentada;
        p.validaAte = validaAte;
        p.despachoNumero = despachoNumero;
        p.despachoData = despachoData;
        p.motivoIndeferimento = motivoIndeferimento;
        return p;
    }

    public void autorizar(String despachoNumero, LocalDate despachoData) {
        exigirPedida();
        if (despachoNumero == null || despachoNumero.isBlank())
            throw invalido("Indique o número do despacho que autoriza a permanência.");
        this.despachoNumero = despachoNumero.trim();
        this.despachoData = despachoData;
        this.estado = Estado.AUTORIZADA;
    }

    public void indeferir(String motivo) {
        exigirPedida();
        if (motivo == null || motivo.isBlank()) throw invalido("Indeferir a permanência exige o motivo.");
        this.motivoIndeferimento = motivo.trim();
        this.estado = Estado.INDEFERIDA;
    }

    public boolean isAutorizada() {
        return estado == Estado.AUTORIZADA;
    }

    private void exigirPedida() {
        if (estado != Estado.PEDIDA)
            throw IgrpResponseStatusException.conflict("Esta prorrogação já foi decidida.");
    }

    private static IgrpResponseStatusException invalido(String m) {
        return IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, m);
    }
}
