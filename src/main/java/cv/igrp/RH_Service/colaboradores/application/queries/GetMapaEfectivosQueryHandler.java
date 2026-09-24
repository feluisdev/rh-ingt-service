package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.CargoEfectivosDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MapaEfectivosDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.UnidadeEfectivosDTO;
import cv.igrp.RH_Service.colaboradores.application.services.MapaEfectivosService;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GetMapaEfectivosQueryHandler implements QueryHandler<GetMapaEfectivosQuery, ResponseEntity<MapaEfectivosDTO>> {

    private final MapaEfectivosService mapaEfectivosService;

    @IgrpQueryHandler
    public ResponseEntity<MapaEfectivosDTO> handle(GetMapaEfectivosQuery query) {
        return ResponseEntity.ok(dto(mapa(mapaEfectivosService, query.getUnidadeId(), query.getIncluirSubunidades())));
    }

    static MapaEfectivosService.Mapa mapa(MapaEfectivosService service, String unidadeId, Boolean subunidades) {
        UUID unidade = null;
        if (unidadeId != null && !unidadeId.isBlank()) {
            try {
                unidade = UUID.fromString(unidadeId.trim());
            } catch (IllegalArgumentException e) {
                throw IgrpResponseStatusException.of(HttpStatus.UNPROCESSABLE_ENTITY, "unidadeId inválido: " + unidadeId + ".");
            }
        }
        return service.mapa(unidade, subunidades == null || subunidades);
    }

    static MapaEfectivosDTO dto(MapaEfectivosService.Mapa m) {
        var t = m.totais();
        return new MapaEfectivosDTO(m.data(), m.raiz().getId().getStringValor(), m.raiz().getName(), m.incluirSubunidades(),
                t.lugares(), t.providos(), t.vagos(), t.congelados(),
                m.unidades().stream().map(u -> new UnidadeEfectivosDTO(u.unidade().getId().getStringValor(), u.unidade().getCode(),
                        u.unidade().getName(), u.totais().lugares(), u.totais().providos(), u.totais().vagos(), u.totais().congelados(),
                        u.cargos().stream().map(c -> new CargoEfectivosDTO(c.carreira(), c.categoria(), c.foraDeGrelha(),
                                c.contagem().lugares(), c.contagem().providos(), c.contagem().vagos(), c.contagem().congelados())).toList()
                )).toList());
    }
}
