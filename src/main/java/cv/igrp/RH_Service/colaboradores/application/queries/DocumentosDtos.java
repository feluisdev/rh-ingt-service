package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.DocumentoEmitidoDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PedidoDeclaracaoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.DeclaracoesService;
import cv.igrp.RH_Service.colaboradores.domain.models.DocumentoEmitido;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;

/** Dos documentos emitidos e pedidos de declaração para os DTOs. */
public final class DocumentosDtos {

    private DocumentosDtos() {}

    public static DocumentoEmitidoDTO dto(DocumentoEmitido d) {
        if (d == null) return null;
        return new DocumentoEmitidoDTO(d.getId().getStringValor(), d.getTipo().name(), d.getNumero(),
                d.getFuncionarioId() != null ? d.getFuncionarioId().getStringValor() : null, d.getTitulo(), d.getEmitidoEm(),
                d.getCodigoVerificacao(), d.getImpressaoDigital(), d.isAnulado(), d.getAnuladoEm(), d.getMotivoAnulacao());
    }

    public static PedidoDeclaracaoDTO dto(DeclaracoesService.Pedido r, Funcionario f) {
        var p = r.pedido();
        return new PedidoDeclaracaoDTO(p.getId().getStringValor(), p.getFuncionarioId().getStringValor(),
                f != null ? f.getNumeroFuncionario() : null, f != null ? f.getNomeCompleto() : null, p.getTipo().name(),
                p.getFinalidade(), p.getEstado().name(), p.isPedidoPeloProprio(), p.getDataPedido(), p.getMotivoRecusa(),
                dto(r.documento()));
    }
}
