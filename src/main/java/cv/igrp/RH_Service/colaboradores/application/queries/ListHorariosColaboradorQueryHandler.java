package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.HorarioColaboradorResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.domain.models.Horario;
import cv.igrp.RH_Service.parametrizacoes.domain.repository.HorarioRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/** O histórico dos horários atribuídos ao colaborador, do mais antigo para o mais recente. */
@Component
@RequiredArgsConstructor
public class ListHorariosColaboradorQueryHandler
        implements QueryHandler<ListHorariosColaboradorQuery, ResponseEntity<List<HorarioColaboradorResponseDTO>>> {

    private final HorarioColaboradorService horarioColaboradorService;
    private final HorarioRepository horarioRepository;

    @IgrpQueryHandler
    public ResponseEntity<List<HorarioColaboradorResponseDTO>> handle(ListHorariosColaboradorQuery query) {
        var historico = horarioColaboradorService.historico(FuncionarioId.from(query.getFuncionarioId()));
        return ResponseEntity.ok(historico.stream().map(h -> new HorarioColaboradorResponseDTO(
                h.getId().getStringValor(),
                h.getHorarioId().getStringValor(),
                horarioRepository.findById(h.getHorarioId()).map(Horario::getNome).orElse(null),
                h.getRegimePrestacao().name(),
                h.getDataInicio(),
                h.getDataFim())).toList());
    }
}
