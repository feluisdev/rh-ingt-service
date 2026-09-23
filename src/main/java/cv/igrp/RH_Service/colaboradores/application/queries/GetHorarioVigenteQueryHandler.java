package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.HorarioVigenteResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.services.HorarioColaboradorService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.RH_Service.parametrizacoes.infrastructure.mappers.HorarioMapper;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** O horário que vale para o colaborador numa data, e de onde vem. */
@Component
@RequiredArgsConstructor
public class GetHorarioVigenteQueryHandler implements QueryHandler<GetHorarioVigenteQuery, ResponseEntity<HorarioVigenteResponseDTO>> {

    private final HorarioColaboradorService horarioColaboradorService;
    private final HorarioMapper horarioMapper;

    @IgrpQueryHandler
    public ResponseEntity<HorarioVigenteResponseDTO> handle(GetHorarioVigenteQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        LocalDate data = query.getData() != null ? query.getData() : LocalDate.now();
        var v = horarioColaboradorService.vigente(funcionarioId, data);
        return ResponseEntity.ok(new HorarioVigenteResponseDTO(
                funcionarioId.getStringValor(),
                data,
                v.origem().name(),
                v.regime() != null ? v.regime().name() : null,
                v.atribuicao() != null ? v.atribuicao().getId().getStringValor() : null,
                horarioMapper.toDTO(v.horario())));
    }
}
