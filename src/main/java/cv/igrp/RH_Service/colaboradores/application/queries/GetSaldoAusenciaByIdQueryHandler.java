package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.SaldoAusenciaResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.SaldoAusenciaRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.SaldoAusenciaId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.SaldoAusenciaMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component("colabsGetSaldoAusenciaByIdQueryHandler")
@RequiredArgsConstructor
public class GetSaldoAusenciaByIdQueryHandler
        implements QueryHandler<GetSaldoAusenciaByIdQuery, ResponseEntity<SaldoAusenciaResponseDTO>> {

    private final SaldoAusenciaRepository saldoRepository;
    private final SaldoAusenciaMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<SaldoAusenciaResponseDTO> handle(GetSaldoAusenciaByIdQuery query) {
        var saldo = saldoRepository.findById(SaldoAusenciaId.from(query.getSaldoId()))
                .orElseThrow(() -> IgrpResponseStatusException.notFound("Saldo não encontrado: " + query.getSaldoId()));
        return ResponseEntity.ok(mapper.toDTO(saldo));
    }
}
