package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDocumentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DocumentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.RH_Service.shared.domain.service.CurrentEmployeeResolver;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.UUID;

@Component("colabsGetMeDocumentsQueryHandler")
@RequiredArgsConstructor
public class GetMeDocumentsQueryHandler
        implements QueryHandler<GetMeDocumentsQuery, ResponseEntity<WrapperListaDocumentoDTO>> {

    private final CurrentEmployeeResolver currentEmployeeResolver;
    private final FuncionarioRepository funcionarioRepository;
    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDocumentoDTO> handle(GetMeDocumentsQuery query) {
        var funcionarioId = currentEmployeeResolver.resolve();

        var funcionario = funcionarioRepository.findById(funcionarioId)
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Funcionário não encontrado: " + funcionarioId.getStringValor()));

        if (!Boolean.TRUE.equals(funcionario.getIsActive()))
            throw IgrpResponseStatusException.of(HttpStatus.FORBIDDEN, "Acesso negado: colaborador inactivo.");

        var filter = new DocumentoFilter();
        filter.setActive(true);
        if (StringUtils.hasText(query.getDocumentTypeId()))
            filter.setDocumentTypeId(UUID.fromString(query.getDocumentTypeId()));

        var list = documentoRepository.findAllByReference("FUNCIONARIO", funcionarioId.getValor(), filter)
                .stream().map(mapper::toDTO).toList();

        var wrapper = new WrapperListaDocumentoDTO();
        wrapper.setContent(new ArrayList<>(list));
        wrapper.setTotalElements(list.size());
        wrapper.setPageNumber(0);
        wrapper.setPageSize(list.size());
        wrapper.setTotalPages(list.size() == 0 ? 0 : 1);
        wrapper.setFirst(true);
        wrapper.setLast(true);
        return ResponseEntity.ok(wrapper);
    }
}
