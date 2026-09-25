package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ProcessoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.ProrrogacaoPermanenciaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.SituacaoAposentacaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AposentacaoService;
import cv.igrp.RH_Service.colaboradores.domain.models.ProcessoAposentacao;
import cv.igrp.RH_Service.colaboradores.domain.models.ProrrogacaoPermanencia;

import java.util.ArrayList;
import java.util.List;

/** Do domínio da aposentação para os DTOs. */
public final class AposentacaoDtos {

    private AposentacaoDtos() {}

    public static ProcessoAposentacaoDTO dto(ProcessoAposentacao p) {
        if (p == null) return null;
        return new ProcessoAposentacaoDTO(p.getId().getStringValor(), p.getFuncionarioId().getStringValor(),
                p.getModalidade().name(), p.getEstado().name(), p.getIniciativa().name(), p.getDataPedido(),
                p.getDataPrevista(), p.getFundamentacao(), p.isAcordoFuncionario(), p.getDespachoNumero(),
                p.getDespachoData(), p.getMotivoIndeferimento(), p.getDataDesligacao(), p.getPercentagemPrestacao(),
                p.getDataAposentacao(), p.getMotivoCancelamento());
    }

    public static ProrrogacaoPermanenciaDTO dto(ProrrogacaoPermanencia p) {
        return new ProrrogacaoPermanenciaDTO(p.getId().getStringValor(), p.getFuncionarioId().getStringValor(),
                p.getEstado().name(), p.getDataPedido(), p.getPropostaFundamentada(), p.getValidaAte(),
                p.getDespachoNumero(), p.getDespachoData(), p.getMotivoIndeferimento());
    }

    /** Com {@code completo}, leva o histórico de processos e prorrogações (a ficha); sem ele, a linha do relatório. */
    public static SituacaoAposentacaoDTO dto(AposentacaoService.Situacao s, boolean completo) {
        var f = s.funcionario();
        var t = s.tempoServico();
        return new SituacaoAposentacaoDTO(f.getId().getStringValor(), f.getNumeroFuncionario(), f.getNomeCompleto(),
                f.getDataNascimento(), s.idade(), s.faz65(), s.faz70(), s.prorrogadoAte(), s.limiteEfectivo(),
                t != null ? t.diasContados() : 0, t != null ? t.anos() : 0, t != null ? t.meses() : 0, t != null ? t.dias() : 0,
                s.completa34Anos(), s.preAposentacaoPossivel(), s.podeAntecipada(), s.podePreAposentacao(),
                s.processoEmCurso().map(AposentacaoDtos::dto).orElse(null),
                completo ? s.processos().stream().map(AposentacaoDtos::dto).toList() : new ArrayList<>(),
                completo ? s.prorrogacoes().stream().map(AposentacaoDtos::dto).toList() : new ArrayList<>(),
                new ArrayList<>(s.alertas()));
    }

    static List<SituacaoAposentacaoDTO> linhas(List<AposentacaoService.Situacao> situacoes) {
        return situacoes.stream().map(s -> dto(s, false)).toList();
    }
}
