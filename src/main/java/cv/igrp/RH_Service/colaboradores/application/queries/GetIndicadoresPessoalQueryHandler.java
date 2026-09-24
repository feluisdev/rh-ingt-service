package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.ContagemDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.IndicadoresPessoalDTO;
import cv.igrp.RH_Service.colaboradores.application.services.IndicadoresPessoalService;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetIndicadoresPessoalQueryHandler implements QueryHandler<GetIndicadoresPessoalQuery, ResponseEntity<IndicadoresPessoalDTO>> {

    private final IndicadoresPessoalService indicadoresPessoalService;

    @IgrpQueryHandler
    public ResponseEntity<IndicadoresPessoalDTO> handle(GetIndicadoresPessoalQuery query) {
        UUID unidade = null;
        if (query.getUnidadeId() != null && !query.getUnidadeId().isBlank()) {
            try {
                unidade = UUID.fromString(query.getUnidadeId().trim());
            } catch (IllegalArgumentException e) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "unidadeId inválido: " + query.getUnidadeId() + ".");
            }
        }
        var i = indicadoresPessoalService.indicadores(unidade, query.getIncluirSubunidades() == null || query.getIncluirSubunidades(),
                query.getAno());
        return ResponseEntity.ok(new IndicadoresPessoalDTO(i.ano(), i.referencia(), i.raiz().getId().getStringValor(),
                i.raiz().getName(), i.incluirSubunidades(), i.efectivos(), lista(i.porGenero()), lista(i.porEscalaoEtario()),
                lista(i.porTipoContrato()), lista(i.porCarreira()), lista(i.porUnidade()), i.entradas(), i.saidas(),
                i.diasFalta(), i.diasUteisPotenciais(), i.taxaAbsentismo(), i.minutosSuplementares(),
                BigDecimal.valueOf(i.minutosSuplementares()).divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)));
    }

    private static List<ContagemDTO> lista(Map<String, Integer> m) {
        return m.entrySet().stream().map(e -> new ContagemDTO(e.getKey(), e.getValue())).toList();
    }
}
