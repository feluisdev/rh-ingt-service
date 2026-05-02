package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.DocumentoId;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.RH_Service.shared.domain.service.DocumentoService;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("colabsGetMeDocumentDownloadUrlQueryHandler")
@RequiredArgsConstructor
public class GetMeDocumentDownloadUrlQueryHandler
        implements QueryHandler<GetMeDocumentDownloadUrlQuery, ResponseEntity<Map<String, ?>>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoService documentoService;

    @IgrpQueryHandler
    public ResponseEntity<Map<String, ?>> handle(GetMeDocumentDownloadUrlQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var documento = documentoRepository.findById(DocumentoId.from(query.getDocumentoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Documento não encontrado: " + query.getDocumentoId()));

        if (!documento.getReferenceId().equals(funcionarioId))
            throw IgrpResponseStatusException.notFound("Documento não encontrado: " + query.getDocumentoId());

        var presignedResponse = documentoService.getPresignedLink(documento.getFileKey());
        var downloadUrl = presignedResponse.getBody() != null ? presignedResponse.getBody().getUrl() : "";
        return ResponseEntity.ok(Map.of("downloadUrl", downloadUrl != null ? downloadUrl : ""));
    }
}
