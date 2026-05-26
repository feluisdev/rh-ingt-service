package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.HistoricoEstadoColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.domain.repository.HistoricoEstadoColaboradorRepository;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.colaboradores.infrastructure.mappers.HistoricoEstadoColaboradorMapper;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.WorkerStateRepository;
import cv.igrp.RH_Service.parametrizacoes.domain.valueobject.WorkerStateId;
import cv.igrp.RH_Service.parametrizacoes.application.port.OptionLookupPort;
import cv.igrp.RH_Service.parametrizacoes.domain.models.OptionCcode;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetHistoricoEstadoColaboradorQueryHandler
        implements QueryHandler<GetHistoricoEstadoColaboradorQuery, ResponseEntity<List<HistoricoEstadoColaboradorResponseDTO>>> {

    private final HistoricoEstadoColaboradorRepository historicoRepository;
    private final HistoricoEstadoColaboradorMapper mapper;
    private final WorkerStateRepository workerStateRepository;
    private final OptionLookupPort optionLookupPort;

    @IgrpQueryHandler
    public ResponseEntity<List<HistoricoEstadoColaboradorResponseDTO>> handle(GetHistoricoEstadoColaboradorQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var lista = historicoRepository.findAllByFuncionarioId(funcionarioId);

        var result = lista.stream().map(h -> {
            var dto = mapper.toDTO(h);

            if (h.getEstadoAnteriorId() != null) {
                workerStateRepository.findById(WorkerStateId.from(h.getEstadoAnteriorId()))
                        .ifPresent(ws -> dto.setEstadoAnteriorDescricao(ws.getDescription()));
            }
            workerStateRepository.findById(WorkerStateId.from(h.getEstadoNovoId()))
                    .ifPresent(ws -> dto.setEstadoNovoDescricao(ws.getDescription()));

            if (h.getMotivoCkey() != null) {
                optionLookupPort.findByCcodeAndCkey(OptionCcode.WORKER_STATE_REASON.getCode(), h.getMotivoCkey())
                        .ifPresent(opt -> dto.setMotivoDescricao(opt.cvalue()));
            }

            return dto;
        }).toList();

        return ResponseEntity.ok(result);
    }
}
