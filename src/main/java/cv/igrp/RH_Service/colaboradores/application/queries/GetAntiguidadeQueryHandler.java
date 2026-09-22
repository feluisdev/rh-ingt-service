package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.AntiguidadeResponseDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.PeriodoAntiguidadeDTO;
import cv.igrp.RH_Service.colaboradores.application.services.AntiguidadeService;
import cv.igrp.RH_Service.colaboradores.domain.valueobject.FuncionarioId;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Lê o tempo de serviço de um colaborador — ver {@link AntiguidadeService} para as regras.
 *
 * <p>É uma consulta e não um comando porque a antiguidade <b>não se guarda</b>: deriva-se do
 * percurso, e é recalculada a cada leitura. Um número gravado ficaria velho na primeira vez que
 * alguém corrigisse uma data do passado.
 */
@Component
@RequiredArgsConstructor
public class GetAntiguidadeQueryHandler
        implements QueryHandler<GetAntiguidadeQuery, ResponseEntity<AntiguidadeResponseDTO>> {

    private final AntiguidadeService antiguidadeService;

    @IgrpQueryHandler
    public ResponseEntity<AntiguidadeResponseDTO> handle(GetAntiguidadeQuery query) {
        var funcionarioId = FuncionarioId.from(query.getFuncionarioId());
        var antiguidade = antiguidadeService.calcular(funcionarioId, query.getAte());

        List<PeriodoAntiguidadeDTO> periodos = antiguidade.periodosDescontados().stream()
                .map(p -> new PeriodoAntiguidadeDTO(p.inicio(), p.fim(), p.dias(), p.motivo()))
                .toList();

        return ResponseEntity.ok(new AntiguidadeResponseDTO(
                query.getFuncionarioId(),
                antiguidade.dataInicio(),
                antiguidade.dataReferencia(),
                antiguidade.diasTotais(),
                antiguidade.diasDescontados(),
                antiguidade.diasContados(),
                antiguidade.anos(),
                antiguidade.meses(),
                antiguidade.dias(),
                periodos));
    }
}
