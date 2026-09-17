package cv.igrp.RH_Service.estrutura.application.queries;

import cv.igrp.RH_Service.estrutura.application.dto.PositionResponseDTO;
import cv.igrp.RH_Service.estrutura.application.dto.WrapperListaPositionDTO;
import cv.igrp.RH_Service.estrutura.application.port.PositionOccupancyPort;
import cv.igrp.RH_Service.estrutura.domain.models.Position;
import cv.igrp.RH_Service.estrutura.domain.repository.PositionRepository;
import cv.igrp.RH_Service.estrutura.infrastructure.mappers.PositionMapper;
import cv.igrp.RH_Service.shared.domain.exceptions.IgrpResponseStatusException;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Lista os Lugares de uma unidade (Mapa de Pessoal), com a ocupacao resolvida.
 *
 * <p>Ate 2026-09-17 esta query devolvia {@code ocupado = null} em cada Lugar e
 * {@code ocupados = 0, vagas = 0} no wrapper, porque a ocupacao e de colaboradores/ e este
 * handler nao tinha por onde a perguntar. O frontend lia esses campos como se fossem
 * resposta -- o Mapa de Pessoal mostrava todos os Lugares como "Vago" e o picker de
 * admissao oferecia Lugares ja providos, que rebentavam depois em 422 no AssignmentService.
 * A ocupacao passa a vir do {@link PositionOccupancyPort}, numa consulta so.
 *
 * <p>Os numeros sao deliberadamente identicos aos de
 * {@code colaboradores.GetVagasListaUnidadeQueryHandler}: {@code dotacao} conta os Lugares
 * ocupaveis ({@link Position#podeSerOcupado()}), nao todas as linhas -- um Lugar extinto ou
 * congelado aparece na listagem mas nao e uma cadeira do quadro. As duas APIs nao podem
 * voltar a discordar sobre a mesma unidade.
 */
@Component
@RequiredArgsConstructor
public class GetPositionsByUnidadeQueryHandler
        implements QueryHandler<GetPositionsByUnidadeQuery, ResponseEntity<WrapperListaPositionDTO>> {

    private final PositionRepository positionRepository;
    private final PositionOccupancyPort positionOccupancyPort;
    private final PositionMapper mapper;

    @IgrpQueryHandler
    public ResponseEntity<WrapperListaPositionDTO> handle(GetPositionsByUnidadeQuery query) {
        if (query.getUnidadeId() == null || query.getUnidadeId().isBlank())
            throw IgrpResponseStatusException.badRequest("A unidade é obrigatória.");

        UUID unidadeId = UUID.fromString(query.getUnidadeId());
        List<Position> lugares = positionRepository.findByUnidade(unidadeId);

        Set<UUID> ocupados = positionOccupancyPort.ocupados(
                lugares.stream().map(p -> p.getId().getValor()).toList());

        List<PositionResponseDTO> content = lugares.stream()
                .map(p -> {
                    PositionResponseDTO dto = mapper.toDTO(p);
                    dto.setOcupado(ocupados.contains(p.getId().getValor()));
                    return dto;
                })
                .toList();

        List<Position> ocupaveis = lugares.stream().filter(Position::podeSerOcupado).toList();
        int dotacao = ocupaveis.size();
        int providos = (int) ocupaveis.stream()
                .filter(p -> ocupados.contains(p.getId().getValor()))
                .count();

        WrapperListaPositionDTO wrapper = new WrapperListaPositionDTO();
        wrapper.setContent(content);
        wrapper.setTotalElements(content.size());
        wrapper.setDotacao(dotacao);
        wrapper.setOcupados(providos);
        wrapper.setVagas(dotacao - providos);
        return ResponseEntity.ok(wrapper);
    }
}
