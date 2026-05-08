package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.WrapperListaDocumentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.filter.DocumentoFilter;
import cv.igrp.RH_Service.colaboradores.domain.repository.DocumentoRepository;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.DocumentoMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component("colabsGetDocumentosSubRecursoQueryHandler")
@RequiredArgsConstructor
public class GetDocumentosSubRecursoQueryHandler
        implements QueryHandler<GetDocumentosSubRecursoQuery, ResponseEntity<WrapperListaDocumentoDTO>> {

    private final DocumentoRepository documentoRepository;
    private final DocumentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaDocumentoDTO> handle(GetDocumentosSubRecursoQuery query) {
        var filter = new DocumentoFilter();
        filter.setDocumentTypeId(query.getDocumentTypeId());
        filter.setActive(query.getActive());

        var list = documentoRepository
                .findAllByReference(query.getReferenceEntity(), query.getReferenceId(), filter)
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
