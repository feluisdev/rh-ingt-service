package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.VerificacaoDocumentoDTO;
import cv.igrp.RH_Service.colaboradores.application.services.EmissaoDocumentosService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * Verificação pública (sem autenticação): quem recebe uma declaração confirma que é verdadeira. Responde
 * só o essencial — tipo, número, data, titular e se foi anulado —; um código que não existe dá
 * {@code valido: false}, não 404, para não se distinguir de um documento anulado por tentativa e erro.
 */
@Component
@RequiredArgsConstructor
public class VerificarDocumentoQueryHandler implements QueryHandler<VerificarDocumentoQuery, ResponseEntity<VerificacaoDocumentoDTO>> {

    private final EmissaoDocumentosService emissao;

    @IgrpQueryHandler
    public ResponseEntity<VerificacaoDocumentoDTO> handle(VerificarDocumentoQuery q) {
        var v = emissao.verificar(q.getCodigo());
        var d = v.documento();
        return ResponseEntity.ok(new VerificacaoDocumentoDTO(q.getCodigo(), v.existe() && !v.anulado(), v.anulado(),
                d != null ? d.getTipo().name() : null, d != null ? d.getNumero() : null, d != null ? d.getTitulo() : null,
                d != null ? d.getEmitidoEm() : null, v.titular()));
    }
}
