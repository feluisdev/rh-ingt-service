package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ReciboVencimentoDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.ReciboVencimentoRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.ReciboVencimentoId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.ReciboVencimentoMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetReciboVencimentoQueryHandler")
@RequiredArgsConstructor
public class GetReciboVencimentoQueryHandler
        implements QueryHandler<GetReciboVencimentoQuery, ResponseEntity<ReciboVencimentoDTO>> {

    private final ReciboVencimentoRepository reciboVencimentoRepository;
    private final ReciboVencimentoMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<ReciboVencimentoDTO> handle(GetReciboVencimentoQuery query) {
        var recibo = reciboVencimentoRepository.findById(ReciboVencimentoId.from(query.getReciboId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound(
                        "Recibo não encontrado: " + query.getReciboId()));

        return ResponseEntity.ok(mapper.toDTO(recibo));
    }
}
