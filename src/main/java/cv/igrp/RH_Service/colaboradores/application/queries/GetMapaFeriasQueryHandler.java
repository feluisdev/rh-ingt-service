package cv.igrp.RH_Service.colaboradores.application.queries;

import cv.igrp.RH_Service.colaboradores.application.dto.MapaFeriasLinhaDTO;
import cv.igrp.RH_Service.colaboradores.application.dto.MapaFeriasResponseDTO;
import cv.igrp.RH_Service.parametrizacoes.application.services.ParametrosFeriasService;
import cv.igrp.RH_Service.colaboradores.domain.models.FeriasDoAno;
import cv.igrp.RH_Service.colaboradores.domain.models.Funcionario;
import cv.igrp.RH_Service.colaboradores.domain.models.MapaFerias;
import cv.igrp.RH_Service.colaboradores.domain.repository.FeriasDoAnoRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.FuncionarioRepository;
import cv.igrp.RH_Service.colaboradores.domain.repository.MapaFeriasRepository;
import cv.igrp.framework.core.domain.QueryHandler;
import cv.igrp.framework.stereotype.IgrpQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * O mapa de férias do ano (art. 6.º n.º 1): as marcações de cada colaborador e, à parte, quem ainda
 * não tem nenhuma — a lista com que o dirigente trabalha quando não há acordo (art. 5.º n.º 5).
 */
@Component
@RequiredArgsConstructor
public class GetMapaFeriasQueryHandler implements QueryHandler<GetMapaFeriasQuery, ResponseEntity<MapaFeriasResponseDTO>> {

    private final FeriasDoAnoRepository feriasDoAnoRepository;
    private final MapaFeriasRepository mapaFeriasRepository;
    private final FuncionarioRepository funcionarioRepository;
    private final ParametrosFeriasService parametrosFerias;

    @IgrpQueryHandler
    public ResponseEntity<MapaFeriasResponseDTO> handle(GetMapaFeriasQuery query) {
        int ano = query.getAno();
        List<FeriasDoAno> marcadas = feriasDoAnoRepository.findAllComMarcacaoByAno(ano);
        List<UUID> semMarcacao = feriasDoAnoRepository.findFuncionariosActivosSemMarcacao(ano);
        Set<UUID> comPreferencia = new HashSet<>(feriasDoAnoRepository.findFuncionariosComPreferencia(ano));

        List<UUID> ids = new ArrayList<>(semMarcacao);
        marcadas.forEach(f -> ids.add(f.getFuncionarioId().getValor()));
        Map<UUID, Funcionario> pessoas = funcionarioRepository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(f -> f.getId().getValor(), Function.identity(), (a, b) -> a));

        var r = new MapaFeriasResponseDTO();
        r.setAno(ano);
        r.setPrazoElaboracao(parametrosFerias.vigenteEm(ano).prazoMapa(ano));
        r.setPublicadoEm(mapaFeriasRepository.findByAno(ano).map(MapaFerias::getPublicadoEm).orElse(null));

        r.setLinhas(marcadas.stream().map(f -> {
            var l = linha(f.getFuncionarioId().getValor(), pessoas);
            l.setOrigem(f.getOrigem().name());
            l.setPeriodos(GetFeriasDoAnoQueryHandler.dtos(f.getMarcacao()));
            l.setTotalMarcado(f.totalMarcado());
            l.setTemPreferencia(f.temPreferencia());
            return l;
        }).sorted(Comparator.comparing(MapaFeriasLinhaDTO::getNumeroFuncionario,
                Comparator.nullsLast(Comparator.naturalOrder()))).toList());

        r.setSemMarcacao(semMarcacao.stream().map(id -> {
            var l = linha(id, pessoas);
            l.setTemPreferencia(comPreferencia.contains(id));
            return l;
        }).toList());
        return ResponseEntity.ok(r);
    }

    private static MapaFeriasLinhaDTO linha(UUID id, Map<UUID, Funcionario> pessoas) {
        var l = new MapaFeriasLinhaDTO();
        l.setFuncionarioId(id.toString());
        var f = pessoas.get(id);
        if (f != null) {
            l.setNumeroFuncionario(f.getNumeroFuncionario());
            l.setNome(f.getNomeCompleto());
        }
        return l;
    }
}
